---
applyTo: "src/test/**/*"
---

## Unit tests requirements
- use Behavior-Driven Development
- use JUnit 5 (Jupiter)
- use given, when, then convention with one line of space between each section; within the section do not use empty lines if not needed for clarity
- use @DisplayName annotation with given, when and then sections. Each section should start at new line and end with comma, use java text blocks for strings
- for test method name you can use just "should"
- usually write 2 or 3 "normal scenarios" (happy and unhappy paths) as well as corner cases if you figure out any
- for instances of Data Loaders and Financial Instruments use example business values:
Data Loader example: 
id = 3 (or other random long)
uuid = use UUID.randomUUID().toString()
lastConnectedOn, lastInstantOfFinancialInstrumentsAssignment, lastLoadStatusUpdate= some mocked Instant
active = true / false
status = TOO_HIGH, TOO_LOW or BALANCED

Financial Instrument example:
id = 4 (or other random long)
name = Microsoft Inc.
symbol = MSFT (ticker)
dataLoaderId = some id of mocked Data Loader or null

Basically, these fields and their types are available in classes of com.piotrgrochowiecki.manager.domain.model package

In case of doubts, ask before proceeding

## Build and Test Tools
- Use Maven Wrapper (`mvnw`) instead of `mvn` for running tests