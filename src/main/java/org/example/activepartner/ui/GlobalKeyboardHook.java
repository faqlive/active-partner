package org.example.activepartner.ui;

import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

final class GlobalKeyboardHook implements AutoCloseable {
    private NativeKeyListener listener;
    private boolean registeredHere;

    boolean isActive() {
        return listener != null && GlobalScreen.isNativeHookRegistered();
    }

    void start(BooleanSupplier canAdd, BooleanSupplier canStop, Runnable addPoint, Runnable stop,
               Runnable manualInteraction) throws NativeHookException {
        if (listener != null) {
            return;
        }

        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);
        if (!GlobalScreen.isNativeHookRegistered()) {
            GlobalScreen.registerNativeHook();
            registeredHere = true;
        }

        listener = new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent event) {
                if (event.getKeyCode() == NativeKeyEvent.VC_F8 && canAdd.getAsBoolean()) {
                    addPoint.run();
                } else if (event.getKeyCode() == NativeKeyEvent.VC_F9 && canStop.getAsBoolean()) {
                    stop.run();
                } else {
                    manualInteraction.run();
                }
            }

            @Override
            public void nativeKeyTyped(NativeKeyEvent event) {
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent event) {
            }
        };
        GlobalScreen.addNativeKeyListener(listener);
    }

    @Override
    public void close() throws NativeHookException {
        if (listener != null) {
            GlobalScreen.removeNativeKeyListener(listener);
            listener = null;
        }
        if (registeredHere) {
            GlobalScreen.unregisterNativeHook();
            registeredHere = false;
        }
    }
}
