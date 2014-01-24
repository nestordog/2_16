package ch.algotrader.adapter.fix;

import quickfix.SessionStateListener;

public class NoopSessionStateListener implements SessionStateListener {

    @Override
    public void onConnect() {
    }

    @Override
    public void onDisconnect() {
    }

    @Override
    public void onLogon() {
    }

    @Override
    public void onLogout() {
    }

    @Override
    public void onReset() {
    }

    @Override
    public void onRefresh() {
    }

    @Override
    public void onMissedHeartBeat() {
    }

    @Override
    public void onHeartBeatTimeout() {
    }

}
