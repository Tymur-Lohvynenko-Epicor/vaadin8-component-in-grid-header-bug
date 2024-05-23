package org.example;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of an HTML page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
@Push(value = PushMode.MANUAL, transport = Transport.LONG_POLLING)
public class MyUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();

        Grid<Item> grid = new Grid<>();
        grid.setWidth(600, Unit.PIXELS);

        IntStream.range(1, 5)
                .mapToObj(String::valueOf)
                .forEach(i -> grid.addColumn(item -> item.getName() + i).setCaption(i));

        String columnId = "status-column";

        grid.addComponentColumn(item -> {
            Button statusLink = new Button("status " + item.getName());
            statusLink.addStyleNames(ValoTheme.BUTTON_LINK, ValoTheme.BUTTON_SMALL);
            statusLink.setHeightUndefined();
            return new HorizontalLayout(statusLink);
        })
                .setCaption("Status")
                .setId(columnId);

        grid.getDefaultHeaderRow().getCell(columnId)
                .setComponent(new HorizontalLayout(new Label("Status")));

        grid.setItems(new Item("test-item"));

        layout.addComponents(grid);
        
        setContent(layout);

        CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {}
                    return "";
                })
                .whenComplete((s, thr) -> {
                    UI ui = getUI();
                    ui.access(() -> {
                        grid.getDefaultHeaderRow().getCell(columnId)
                                .setComponent(createLayoutForStatusColumnInHeader());
                        ui.push();
                    });
                });
    }

    private HorizontalLayout createLayoutForStatusColumnInHeader() {
        Button button = new Button();
        button.addStyleNames(
                ValoTheme.BUTTON_SMALL,
                ValoTheme.BUTTON_ICON_ONLY,
                ValoTheme.BUTTON_BORDERLESS
        );
        button.setIcon(VaadinIcons.REFRESH);
        button.addClickListener(clickEvent -> Notification.show("refresh!"));

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Label label = new Label("Status");
        horizontalLayout.addComponentsAndExpand(label);
        horizontalLayout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
        horizontalLayout.addComponent(button);
        horizontalLayout.setComponentAlignment(button, Alignment.MIDDLE_CENTER);
        return horizontalLayout;
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
