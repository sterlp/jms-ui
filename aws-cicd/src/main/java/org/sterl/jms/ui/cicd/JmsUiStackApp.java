package org.sterl.jms.ui.cicd;

import software.amazon.awscdk.core.App;

public class JmsUiStackApp {
    public static void main(final String[] args) {
        App app = new App();

        new JmsUiStackStack(app, "JmsUiStack");

        app.synth();
    }
}
