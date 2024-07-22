package com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.service;

import com.piotrgrochowiecki.financialInstrumentsSubscriptionsManager.domain.model.SubscriptionModel;

public class SubscriptionService {

    public SubscriptionModel subscribe(String dataLoaderUUID) {
        //check if data loader with given uuid is already present in the system
        //if yes, check status healthy/unhealthy based on last connection time (> 5min == status unhealthy)
            //if healthy, 1. add financial instruments from database to the collection
            //2. return subscriptionModel
            //if unhealthy, ???
        //if no,
            //1. save data loader to DB
            //2. fetch financial instruments not registered with any other data loader, assign data_loader_id of just
            // saved DL to these instruments
            //3. return subscriptionModel

        return null;
    }
}
