package com.clubecerto.apps.app.Services;

import com.google.android.libraries.places.api.model.Place;

public class LocationChangedEvent {
    public Place currentPlaces;

    public LocationChangedEvent(Place places) {
        this.currentPlaces = places;
    }

}
