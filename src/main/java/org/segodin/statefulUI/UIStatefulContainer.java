package org.segodin.statefulUI;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.segodin.statefulUI.action.UIAction;
import org.segodin.statefulUI.action.UIActionDispatcher;
import org.segodin.statefulUI.action.UIActionDispatcherAware;
import org.segodin.statefulUI.component.UIComponent;
import org.segodin.statefulUI.component.UIContainerFactory;
import org.segodin.statefulUI.component.UIContainerFactoryAware;
import org.segodin.statefulUI.state.UIStateImpl;
import org.segodin.statefulUI.state.UIStateReducer;
import org.segodin.statefulUI.state.UIStatefulComponent;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holds state, performs state change, and notifies state listeners. Provides injection with proxy classes.
 * To create container component use {@link #createProxyContainer(Class)}.
 * Created container will automatically inject and initialize all future children, but won't create proxies from nested containers.
 * */
public class UIStatefulContainer implements UIContainerFactory, UIActionDispatcher {

    protected UIStateImpl state = new UIStateImpl();
    protected Set<UIStatefulComponent> statefulComponents = new HashSet<>();
    protected Set<UIStateReducer> stateReducers = new HashSet<>();

    @Override
    public <T extends Container> T createProxyContainer(Class<T> containerClass) {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(containerClass);
        factory.setFilter(
                method -> {

                    if (method.getParameterCount() == 0) {
                        return false;
                    }

                    if ( ! Container.class.isAssignableFrom(method.getDeclaringClass())) {
                        return false;
                    }

                    if ( ! Modifier.isPublic(method.getModifiers())) {
                        return false;
                    }

                    if ( ! "add".equals(method.getName())) {
                        return false;
                    }

                    for (Parameter p : method.getParameters()) {
                        if (Component.class.isAssignableFrom(p.getType())) {
                            return true;
                        }
                    }

                    return false;
                }
        );

        try {
            T result = (T) factory.create(new Class<?>[0], new Object[0], new ContainerMethodHandler());
            initializeComponent(result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public  <T extends Component> T initializeComponent(T component) {
        if (UIActionDispatcherAware.class.isAssignableFrom(component.getClass())) {
            UIActionDispatcherAware dispatcherAware = (UIActionDispatcherAware) component;
            dispatcherAware.setUIActionDispatcher(this);
        }

        if (UIContainerFactoryAware.class.isAssignableFrom(component.getClass())) {
            UIContainerFactoryAware factoryAware = (UIContainerFactoryAware) component;
            factoryAware.setUIContainerFactory(this);
        }

        if (UIStateReducer.class.isAssignableFrom(component.getClass())) {
            stateReducers.add((UIStateReducer) component);
        }

        if (UIStatefulComponent.class.isAssignableFrom(component.getClass())) {
            statefulComponents.add((UIStatefulComponent) component);
        }

        if (UIComponent.class.isAssignableFrom(component.getClass())) {
            ((UIComponent) component).initialize();
        }

        return component;
    }

    @Override
    public synchronized void dispatch(UIAction action) {
        for (UIStateReducer reducer : stateReducers) {
            if (reducer.supportActions().contains(action.getId())) {
                Map<String, Object> reducedState = reducer.reduceState(state, action);
                if (reducedState != null) {
                    boolean stateChanged = false;
                    for (Map.Entry<String, Object> entry : reducedState.entrySet()) {
                        Object oldParam = state.getParameter(entry.getKey());
                        if (oldParam == null || !oldParam.equals(entry.getValue())) {
                            state.setParameter(entry.getKey(), entry.getValue());
                            stateChanged = true;
                        }
                    }
                    if (stateChanged) {
                        for (UIStatefulComponent component : statefulComponents) {
                            component.stateChanged(state);
                        }
                    }
                }
                break;
            }
        }
    }

    private class ContainerMethodHandler implements MethodHandler {

        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
            if (args != null) {
                for (Object arg : args) {
                    if (!Container.class.isAssignableFrom(arg.getClass()) &&
                            Component.class.isAssignableFrom(arg.getClass())) {
                        initializeComponent((Component)arg);
                    }
                }
            }
            return proceed.invoke(self, args);
        }
    }

}
