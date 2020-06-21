package org.sterl.jms.ui.cicd;

import java.io.File;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ecr.assets.DockerImageAsset;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;

public class JmsUiStackStack extends Stack {
    public JmsUiStackStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public JmsUiStackStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        
        final File startDir = new File(".");
        final DockerImageAsset asset = DockerImageAsset.Builder.create(this, "sterl/jms-ui-docker")
                    .directory(new File(startDir, "/../app/backend/").toString()).build();

        // we use default port 80 here ...
        ApplicationLoadBalancedFargateService.Builder.create(this, "jms-ui")
            .desiredCount(1)
            .assignPublicIp(true)
            .cpu(256).memoryLimitMiB(512)
            .taskImageOptions(ApplicationLoadBalancedTaskImageOptions.builder()
                    .image(ContainerImage.fromDockerImageAsset(asset))
                    .build())
            .build();
    }
}
