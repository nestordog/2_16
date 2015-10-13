    private transient boolean initialized = false;

    public boolean isInitialized() {
        return this.initialized;
    }

    public void setInitialized() {
        this.initialized = true;
    }

    @Override
    public <R, P> R accept(ch.algotrader.visitor.EntityVisitor<R, ? super P> visitor, P param) {
        return visitor.visit${pojo.getDeclarationName()}(this, param);
    }  