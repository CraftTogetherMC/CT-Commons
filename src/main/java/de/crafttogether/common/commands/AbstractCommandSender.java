package de.crafttogether.common.commands;

public abstract class AbstractCommandSender<S> implements CommandSender {
    protected final S delegate;

    public AbstractCommandSender(S delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractCommandSender<?> that = (AbstractCommandSender<?>) o;
        return this.delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }
}