package it.uniba.di.itps.SNVSimulation.models;

/**
 * Created by acidghost on 13/09/14.
 */
public class Trait {
    public double agreableness;
    public double extroversion;
    public Interests[] interests;

    public Trait(double agreableness, double extroversion, Interests... interests) {
        this.agreableness = agreableness;
        this.extroversion = extroversion;
        this.interests = interests;
    }
}
