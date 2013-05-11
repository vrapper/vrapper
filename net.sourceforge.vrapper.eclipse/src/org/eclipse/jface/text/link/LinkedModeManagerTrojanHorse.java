package org.eclipse.jface.text.link;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.eclipse.jface.text.IDocument;


public class LinkedModeManagerTrojanHorse {
    
    private static class WrappingListener implements ILinkedModeListener {
        private final ILinkedModeListener wrapped;

        public WrappingListener(final ILinkedModeListener wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void left(final LinkedModeModel model, final int flags) {
            System.out.println("WRAPPED-left");
            wrapped.left(model, flags);
        }

        @Override
        public void suspend(final LinkedModeModel model) {
            System.out.println("WRAPPED-suspend");
            wrapped.suspend(model);
        }

        @Override
        public void resume(final LinkedModeModel model, final int flags) {
            System.out.println("WRAPPED-resume");
            wrapped.resume(model, flags);
        }
    }
    
    @SuppressWarnings("rawtypes")
    public static ILinkedModeManager install(final IDocument document) {
        try {
//            final Class<LinkedModeManager> lmmClass = LinkedModeManager.class; // gives illegal access erro :(
            final Class<?> lmmClass = Class.forName("org.eclipse.jface.text.link.LinkedModeManager");
            final Field fgManagers = lmmClass.getDeclaredField("fgManagers");
            final Field fListener = lmmClass.getDeclaredField("fListener");
            fgManagers.setAccessible(true);
            fListener.setAccessible(true);
            
            final Map managers = (Map) fgManagers.get(null);
            if (managers.size() > 0) {
                // existing managers
                System.out.println("Managers=" + managers);
                for (final Object o : managers.values()) {
                    wrapListener(o, fListener);
                }
            } else {
                // none! make our own
                final IDocument[] docs = new IDocument[]{document};
                final Method getMethod = lmmClass.getMethod("getLinkedManager",
                        new Class<?>[]{docs.getClass(), boolean.class});
                getMethod.setAccessible(true);
                final Object o = getMethod.invoke(null, docs, true);
                wrapListener(o, fListener);
            }
        } catch (final Throwable e) {
            System.err.println("Couldn't install LinkedModeManagerTrojanHorse");
            e.printStackTrace();
        }
        
        return null;
    }

    private static void wrapListener(final Object o, final Field fListener) throws IllegalArgumentException, IllegalAccessException {
        final Object listener = fListener.get(o);
        System.out.println("In " + o + " found listener: " + listener);
        final WrappingListener wrapper = new WrappingListener((ILinkedModeListener) listener);
        fListener.set(o, wrapper);
        System.out.println("Wrapped!");
        
    }
    
//    /** 
//     * A singleton should ensure that the {@link #getLinkedManager(IDocument[], boolean)}
//     *  method never has to create a new instance of the old class
//     */
//    static LinkedModeManagerTrojanHorse instance = new LinkedModeManagerTrojanHorse();
//    
//    /**
//     * A shadow of the private fgManagers variable from our parent class
//     */
//    static Map sfgManagers;
//    
//    static {
//        try {
////            final Field fgManagersField = LinkedModeManager.class.getDeclaredField("fgManagers");
////            fgManagersField.setAccessible(true);
////            sfgManagers = (Map) fgManagersField.get(null);
//        } catch (final Throwable e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            VrapperLog.error("Couldn't access fgManagers field");
//        }
//        
//    }
//
//    /**
//     * Overrides any installed LinkedModeManager installed
//     *  on the document with a TrojanHorse version
//     *  
//     * @param document
//     */
//    @SuppressWarnings("unchecked")
//    public static void forceInstall(final IDocument document) {
//        if (sfgManagers != null) {
//            sfgManagers.put(document, instance);
//        }
//    }

//    @Override
//    public boolean nestEnvironment(final LinkedModeModel model, final boolean force) {
//        model.addLinkingListener(new ILinkedModeListener() {
//            
//            @Override
//            public void suspend(final LinkedModeModel model) {
//                System.out.println("TROJAN-suspend");
//            }
//            
//            @Override
//            public void resume(final LinkedModeModel model, final int flags) {
//                System.out.println("TROJAN-resume");
//            }
//            
//            @Override
//            public void left(final LinkedModeModel model, final int flags) {
//                System.out.println("TROJAN-left");
//            }
//        });
//        return super.nestEnvironment(model, force);
//    }
}
