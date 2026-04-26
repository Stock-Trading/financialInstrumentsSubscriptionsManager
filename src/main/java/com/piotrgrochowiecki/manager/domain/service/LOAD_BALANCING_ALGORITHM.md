# Load Balancing Algorithm - Technical Overview

## Table of Contents
1. [High-Level Overview](#high-level-overview)
2. [Execution Schedule](#execution-schedule)
3. [Algorithm Architecture](#algorithm-architecture)
4. [State Model](#state-model)
5. [Detailed Workflows](#detailed-workflows)
6. [Design Principles](#design-principles)
7. [Failure Recovery](#failure-recovery)

---

## High-Level Overview

The Financial Instruments Subscriptions Manager implements a **continuous load balancing algorithm** that maintains optimal distribution of financial instruments (FIs) across a dynamic pool of data loaders (DLs).

### Key Objectives
- **High Availability**: Automatically detect and recover from data loader failures
- **Fair Distribution**: Evenly distribute load across all active data loaders
- **Scalability**: Handle dynamic addition/removal of data loaders without manual intervention
- **Low Latency**: Rapid response to changes through scheduled periodic checks
- **Autonomy**: Entirely automatic with no external intervention required

### Core Concepts

#### Financial Instrument (FI)
A subscription to financial data that must be assigned to exactly one active data loader for continuous monitoring and updates.

#### Data Loader (DL)
A service instance that:
- Subscribes to assigned financial instruments
- Periodically checks in to prove active status
- Maintains a current load status (BALANCED, TOO_LOW, TOO_HIGH, or null)
- Can accommodate a recommended number of financial instruments

---

## Execution Schedule

The algorithm orchestrates five independent scheduled tasks running at different intervals:

```
Timeline (seconds):
0    1    2    3    4    5    6    7    8    9    10   11   12   13
|    |    |    |    |    |    |    |    |    |    |    |    |    |
✓    ✓    ✓    ✓    ✓    ✓    ✓    ✓    ✓    ✓    ✓    ✓    ✓
│UNASSIGN INACTIVEs (1s)
           │    │    │    │    │    │
           CHECK ACTIVE STATE (3s)
                     │    │    │    │    │    │
                     CHECK LOAD STATUS (5s)
                                  │    │    │    │    │    │
                                  ASSIGN UNASSIGNED (10s)
                                                      │    │
                                                      UNASSIGN EXCESS (12s)
```

### Task Execution Order (Priority-Based)

| Priority | Task                         | Interval   | Purpose                                          | Dependencies               |
|----------|------------------------------|------------|--------------------------------------------------|----------------------------|
| 1 (High) | Unassign from Inactive DLs   | 1 second   | **CRÍTICO**: Clean up failed loaders immediately | None                       |
| 2        | Check Active State           | 3 seconds  | Update active status of all DLs                  | None                       |
| 3        | Check Load Status            | 5 seconds  | Calculate current load of each DL                | Active state               |
| 4        | Assign Unassigned FIs        | 10 seconds | Distribute free FIs to available capacity        | Active state + Load status |
| 5        | Unassign from Overloaded DLs | 12 seconds | Rebalance away from overloaded DLs               | Load status                |

### Scheduling Rationale

1. **1-second cleanup** (HIGHEST PRIORITY)
   - Detects and recovers from failures within seconds
   - Frees up instruments for reassignment immediately
   - Prevents stale assignments from accumulating

2. **3-second active check**
   - Establishes baseline health before load operations
   - Must complete before other checks begin

3. **5-second load status check**
   - Determines current distribution and identifies imbalances
   - Must complete before rebalancing decisions

4. **10-second assignment**
   - Distributes freed instruments to available capacity
   - Waits for load status to stabilize slightly

5. **12-second excess unassignment**
   - Fine-tunes distribution after assignments
   - Handles gradual overload accumulation

---

## Algorithm Architecture

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring Application                       │
├─────────────────────────────────────────────────────────────┤
│  FinancialInstrumentBalancingSchedulerService               │
│  (Orchestrates all scheduled tasks)                         │
├┬────────────────────────────────────────────────────────────┤
││  Scheduled Tasks (run continuously & independently)        │
│├─────────────────────────────────────────────────────────────┤
││ ┌──────────────────────────────────────────────────────┐   │
││ │ 1. UnassignFinancialInstrumentFromAllInactive        │   │
││ │    DataLoaderUseCase (every 1s)                     │   │
││ ├──────────────────────────────────────────────────────┤   │
││ │ 2. CheckActiveStateOfDataLoaderUseCase (every 3s)   │   │
││ ├──────────────────────────────────────────────────────┤   │
││ │ 3. CheckDataLoaderLoadStatusUseCase (every 5s)      │   │
││ ├──────────────────────────────────────────────────────┤   │
││ │ 4. AssignUnassignedFinancialInstrumentToDataLoaders │   │
││ │    UseCase (every 10s)                              │   │
││ ├──────────────────────────────────────────────────────┤   │
││ │ 5. UnassignFinancialInstrumentFromDataLoaderWith    │   │
││ │    TooHighLoadStatusUseCase (every 12s)             │   │
││ └──────────────────────────────────────────────────────┘   │
│└────────────────────────────────────────────────────────────┤
├─────────────────────────────────────────────────────────────┤
│  Domain Services & Repositories                             │
│  ├─ DataLoaderRepository                                    │
│  ├─ FinancialInstrumentRepository                           │
│  ├─ Parameters Providers (configuration)                    │
│  └─ TimeService                                             │
├─────────────────────────────────────────────────────────────┤
│  Data Access Layer (JPA)                                    │
│  ├─ DataLoaderEntity / DataLoaderJpaRepository             │
│  └─ FinancialInstrumentEntity / FinancialInstrumentJpa      │
│     Repository                                             │
├─────────────────────────────────────────────────────────────┤
│  Database                                                   │
│  ├─ data_loaders (id, uuid, active, load_status, ...)      │
│  └─ financial_instruments (id, name, data_loader_id, ...)  │
└─────────────────────────────────────────────────────────────┘
```

### Data Relationships

```
┌─────────────┐                    ┌────────────────┐
│  DataLoader │ 1──────────────────N │FinancialInst. │
├─────────────┤                    ├────────────────┤
│ id (PK)     │                    │ id (PK)        │
│ uuid        │                    │ name           │
│ active      │◄───────────────────│ dataLoaderId   │
│ loadStatus  │      (FK)          │ (nullable)     │
│ lastCheckin │                    │ createdOn      │
│ lastUpdate  │                    └────────────────┘
└─────────────┘
```

---

## State Model

### Data Loader State Machine

```
     ┌─────────────────┐
     │   REGISTERED    │
     │ (active=null)   │
     └────────┬────────┘
              │ (first check-in)
              ▼
     ┌─────────────────┐
     │  ACTIVE         │◄──────────────────┐
     │ (active=true)   │    (check-in ok)  │
     │ loadStatus varied│                  │
     └────────┬────────┘                  │
              │                           │
              │ (no check-in > threshold) │
              │                           │
              ▼                           │
     ┌─────────────────┐                  │
     │  INACTIVE       │──────────────────┘
     │ (active=false)  │  (check-in restored)
     │ loadStatus=null │
     └─────────────────┘
```

### Load Status Values (for Active Data Loaders)

| Status       | Meaning         | Assigned FIs | Recommended FIs | Action                    |
|--------------|-----------------|--------------|-----------------|---------------------------|
| **BALANCED** | Optimal load    | = N          | N               | None, stable              |
| **TOO_LOW**  | Underutilized   | < N          | N               | Can accept more FIs       |
| **TOO_HIGH** | Overloaded      | > N          | N               | Should release FIs        |
| **NULL**     | Not yet checked | N/A          | N               | Initial state or inactive |

### Financial Instrument States

```
┌─────────────┐
│ UNASSIGNED  │◄──────────────────────────┐
│ dataLoaderId│ (1. Unassign from inactive)│
│  = null     │ (5. Unassign if overloaded)
└──────┬──────┘                          │
       │                                 │
       │ (4. Assign to available)        │
       ▼                                 │
┌─────────────┐────────────────────────┐
│ ASSIGNED    │ (2. Move to different DL)│
│ dataLoaderId│◄───────────────────────┘
│  = DL_ID    │
└─────────────┘
```

---

## Detailed Workflows

### Workflow 1: Health Check & Cleanup (Every 1s)

**Purpose**: Immediately recover from data loader failures

```
START
  │
  ├─ Find all Data Loaders with active=false
  │
  ├─ For each inactive Data Loader:
  │    ├─ Find all Financial Instruments assigned to it
  │    ├─ Unassign all (set dataLoaderId = null)
  │    └─ Update Data Loader's lastLoadStatusUpdate timestamp
  │
  └─ END (Return to scheduler)

Result: All FIs from failed loaders are freed and can be reassigned
```

**Example Scenario**:
```
Before:
  DL1 (active=false) ┬─ FI_A
                     ├─ FI_B
                     └─ FI_C
  DL2 (active=true)  └─ (empty)

After:
  DL1 (active=false) ┬─ (empty)
                     │
  DL2 (active=true)  └─ (unchanged)
  
Unassigned: FI_A, FI_B, FI_C
```

### Workflow 2: Active State Monitoring (Every 3s)

**Purpose**: Determine which data loaders are still alive

```
START
  │
  ├─ Get current time
  ├─ Calculate threshold_time = now - ACTIVE_THRESHOLD_MS
  │
  ├─ Find all Data Loaders where lastCheckin < threshold_time
  │
  ├─ For each timed-out Data Loader:
  │    ├─ Set active = false
  │    └─ Set loadStatus = null
  │
  └─ END (Return to scheduler)

Result: All non-responsive loaders are marked inactive
        (Cleanup will follow in next 1s cycle)
```

**Configuration Example**:
```
ACTIVE_THRESHOLD_MS = 30_000  // 30 seconds

If DL.lastCheckin < (now - 30s):
  Mark as inactive
```

### Workflow 3: Load Status Assessment (Every 5s)

**Purpose**: Measure current load and identify imbalances

```
START
  │
  ├─ Get all active Data Loaders
  ├─ Order by lastLoadStatusUpdate (oldest first)
  │
  ├─ For each Data Loader:
  │    ├─ Count assigned Financial Instruments
  │    ├─ Determine status:
  │    │   if count == RECOMMENDED    ─ BALANCED
  │    │   if count <  RECOMMENDED    ─ TOO_LOW
  │    │   if count >  RECOMMENDED    ─ TOO_HIGH
  │    ├─ Set loadStatus
  │    └─ Update lastLoadStatusUpdate = now
  │
  └─ END (Return to scheduler)

Result: All DL have current load status reflecting reality
```

**Status Decision Tree**:
```
Assigned FIs: 3, Recommended: 5
  ├─ 3 == 5? NO
  └─ 3 < 5?  YES ─ Status = TOO_LOW ✓
  
Assigned FIs: 7, Recommended: 5
  ├─ 7 == 5? NO
  ├─ 7 < 5?  NO
  └─ 7 > 5?  YES ─ Status = TOO_HIGH ✓
```

### Workflow 4: Assignment to Available Capacity (Every 10s)

**Purpose**: Distribute unassigned instruments to available loaders

```
START
  │
  ├─ Check: Are there any unassigned FIs?
  │   NO  ─ End, all assigned already
  │
  ├─ YES ─ Retrieve:
  │   ├─ Unassigned FIs (up to batch_size)
  │   │    ordered by createdOn (oldest first)
  │   └─ Active DLs with TOO_LOW or NULL status (up to batch_size)
  │        ordered by lastConnectedOn (oldest first)
  │
  ├─ Round-robin assignment:
  │   for i = 0 to FIs.size()-1:
  │      FI[i].dataLoaderId = DLs[i % DLs.size()].id
  │      persist(FI[i])
  │
  └─ END (Return to scheduler)

Result: Unassigned FIs are distributed across available capacity
```

**Round-Robin Distribution Example**:
```
FIs to assign:   [FI_10, FI_11, FI_12, FI_13, FI_14]
Available DLs:   [DL_1, DL_2, DL_3]

Assignment:
  FI_10 ─ FI_10 → DL_1 (10 % 3 = 1 → idx 0)
  FI_11 ─ FI_11 → DL_2 (11 % 3 = 2 → idx 1)
  FI_12 ─ FI_12 → DL_3 (12 % 3 = 0 → idx 2)
  FI_13 ─ FI_13 → DL_1 (13 % 3 = 1 → idx 0)
  FI_14 ─ FI_14 → DL_2 (14 % 3 = 2 → idx 1)

Result:
  DL_1: FI_10, FI_13
  DL_2: FI_11, FI_14
  DL_3: FI_12
```

### Workflow 5: Overload Relief (Every 12s)

**Purpose**: Rebalance by removing excess assignments from overloaded loaders

```
START
  │
  ├─ Find all active DLs with loadStatus = TOO_HIGH
  ├─ Order by lastInstantOfFinancialInstrumentsAssignment
  │    (oldest first ─ fairness: first to overload gets rebalanced first)
  │
  ├─ For each overloaded Data Loader:
  │    ├─ Get all assigned FIs
  │    ├─ Calculate excess = total - RECOMMENDED
  │    │
  │    ├─ if excess > 0:
  │    │    ├─ Skip first RECOMMENDED FIs (keep these)
  │    │    ├─ Unassign remaining excess FIs
  │    │    │    (set dataLoaderId = null)
  │    │    └─ Update DL.lastLoadStatusUpdate = now
  │
  └─ END (Return to scheduler)

Result: Overloaded loaders are brought back to balance
        (Freed FIs will be reassigned in next 10s cycle)
```

**Excess Rebalancing Example**:
```
DL_1 state:
  - Recommended: 3
  - Assigned: 8
  - Excess: 8 - 3 = 5

Before:
  DL_1: [FI_A, FI_B, FI_C, FI_D, FI_E, FI_F, FI_G, FI_H]

Action:
  Keep: [FI_A, FI_B, FI_C]  (first 3)
  Unassign: [FI_D, FI_E, FI_F, FI_G, FI_H]  (remaining 5)

After:
  DL_1: [FI_A, FI_B, FI_C]  (BALANCED)
  Unassigned: [FI_D, FI_E, FI_F, FI_G, FI_H]
```

---

## Design Principles

### 1. **Independent Execution**
- All 5 tasks run independently and asynchronously
- No task waits for another (except dependencies through database state)
- Allows graceful degradation if one task is delayed

### 2. **Short Execution Windows**
- Each task designed to complete quickly (milliseconds to few seconds)
- Prevents blocking or long transaction locks
- Enables rapid convergence to balanced state

### 3. **Batch Processing with Limits**
- Tasks process limited batches per cycle (configurable)
- Prevents overwhelming database or system resources
- Allows proportional scaling with cluster size

### 4. **Ordered Processing for Fairness**
- Tasks order operations by timestamp/ID
- Ensures no data loader or FI is perpetually neglected
- Achieves fair distribution over time

### 5. **Automatic Recovery**
- Every failure scenario (DL death) triggers automatic recovery
- No manual intervention or operator action required
- System self-heals within seconds

### 6. **Stability & Convergence**
- Overlapping tasks create stabilizing feedback loops
- Rapid detection (1s) + gradual rebalancing (12s) = stability
- System converges to balanced state within 12 seconds of any change

---

## Failure Recovery

### Scenario 1: Data Loader Crashes

```
Timeline:
t=0s   ┌─ DL_2 is active with 3 FIs
       │
t=15s  ┌─ DL_2 misses check-in
       │  (but active=true still)
       │
t=30s  ┌─ CheckActiveState detects:
       │    lastCheckin < (now - 30s)
       │  ├─ Sets active = false
       │  └─ Sets loadStatus = null
       │
t+1s   ┌─ UnassignInactive detects inactive DL_2
       │  ├─ Finds all FIs assigned to DL_2
       │  ├─ Unassigns all 3 FIs
       │  └─ FIs become available
       │
t+10s  ┌─ AssignUnassigned picks up freed FIs
       │  ├─ Finds DL_3, DL_4 with TOO_LOW
       │  └─ Distributes the 3 freed FIs
       │
Result: Within 1-10 seconds of DL death, 
        all its FIs have been reassigned!
```

### Scenario 2: Uneven Distribution Detection

```
Initial state:
  DL_1: 2 FIs (TOO_LOW)
  DL_2: 8 FIs (TOO_HIGH)
  Recommended: 5 each

At t=5s (Load Status Check):
  ├─ Identified: DL_1=TOO_LOW, DL_2=TOO_HIGH
  └─ No action this cycle

At t=10s (Assignment):
  ├─ New FI arrives, goes to DL_1
  └─ Now: DL_1: 3, DL_2: 8

At t=12s (Unassign Excess):
  ├─ Detects DL_2 still TOO_HIGH
  ├─ Excess = 8 - 5 = 3
  ├─ Unassigns last 3 FIs from DL_2
  └─ Now: DL_1: 3, DL_2: 5, Unassigned: 3 FIs

At t+10s (Next Assignment):
  ├─ Reassigns 3 freed FIs
  └─ Final: DL_1≈5, DL_2≈5 (balanced)
```

### Scenario 3: New Data Loader Registration

```
Initial:
  DL_1: 5 FIs (BALANCED)
  DL_2: 5 FIs (BALANCED)
  
t=0s: New DL_3 registers and checks in
  ├─ active = true
  └─ loadStatus = null (not yet checked)

t=3s (CheckActiveState):
  ├─ Confirms DL_3 is active
  ├─ No changes needed

t=5s (CheckLoadStatus):
  ├─ Counts: DL_3 has 0 FIs
  ├─ Status = TOO_LOW (0 < 5)
  └─ DL_3 now available for assignments

t=10s (Assignment):
  ├─ Sees DL_3 with TOO_LOW
  ├─ Assigns new/unassigned FIs to DL_3
  └─ DL_3 starts receiving work

Result: New loader is integrated and balanced
        within 15 seconds of registration!
```

---

## Configuration Parameters

Key parameters that affect algorithm behavior:

```properties
# Active state detection
dataloader.active.threshold.milliseconds=30000  # 30s check-in timeout

# Load balancing targets
dataloader.recommended.financial.instruments=5  # Target per loader

# Batch sizes (prevent overwhelming system)
dataloader.batch.size=10                       # DLs per cycle
financial.instrument.batch.size=20             # FIs per cycle
```

---

## Performance Characteristics

### Convergence Time
- **Failure Detection**: ~1-3 seconds
- **Failure Recovery** (reassign FIs): ~10 seconds
- **Full Rebalancing**: ~12 seconds or less
- **New Loader Integration**: ~15 seconds

### Scalability
- Number of DLs: Linear impact on check time
- Number of FIs: Linear impact on check time
- Batch processing prevents O(n²) issues
- Independent task execution prevents bottlenecks

### Resource Usage
- Database: Periodic queries/updates, well-distributed
- Memory: Minimal (batch processing)
- CPU: Low (mostly I/O bound)
- Network: None (local data access only)

---

## Monitoring & Observability

Each use case logs:
- Task execution start/end
- Number of items processed
- State transitions (active/inactive, status changes)
- Exceptions and errors

Example log output:
```
INFO CheckDataLoaderLoadStatusUseCase - Checking load status of dataLoader DataLoaderModel(id=5, ...)
INFO CheckDataLoaderLoadStatusUseCase - Number of assigned Financial Instruments: 6
DEBUG CheckDataLoaderLoadStatusUseCase - Data Loader with id 5, uuid abc-123 has 6 Financial Instruments assigned and its load status is TOO_HIGH
```

### Recommended Metrics
- FI assignment rate (FIs/minute)
- DL failure detection latency (seconds)
- Load balance distribution (std dev of FI counts)
- Unassigned FI count over time
- Task execution duration (ms per cycle)

---

## Conclusion

This load balancing algorithm provides:
- ✅ **Automatic failure recovery** (within 1-10 seconds)
- ✅ **Fair load distribution** (round-robin + ordered processing)
- ✅ **Scalability** (batch processing prevents O(n²))
- ✅ **High availability** (continuous monitoring)
- ✅ **Zero manual intervention** (fully autonomous)
- ✅ **Fast convergence** (all tuned schedules)

The overlapping periodic checks create a self-correcting system that naturally converges to optimal state regardless of external disruptions.

