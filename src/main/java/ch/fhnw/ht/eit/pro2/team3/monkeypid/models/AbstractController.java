package ch.fhnw.ht.eit.pro2.team3.monkeypid.models;

import ch.fhnw.ht.eit.pro2.team3.monkeypid.interfaces.IController;

public abstract class AbstractController implements IController, Cloneable {
    protected double tp;

    @Override
    public void setParasiticTimeConstant(double tp) {
        this.tp = tp;
    }
}