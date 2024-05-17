package de.crafttogether.common.event;

public abstract class AbstractPlayer<S> implements Player {
    protected final S delegate;

    public AbstractPlayer(S delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractPlayer<?> that = (AbstractPlayer<?>) o;
        return this.delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }
}