package io.shunters.grpc.component.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.util.RoundRobinLoadBalancerFactory;
import io.shunters.grpc.component.proto.helloworld.GreeterGrpc;
import io.shunters.grpc.component.proto.helloworld.HelloReply;
import io.shunters.grpc.component.proto.helloworld.HelloRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by mykidong on 2018-01-10.
 */
public class HelloWorldClientWithNameResolver {
    private static Logger log = LoggerFactory.getLogger(HelloWorldClientWithNameResolver.class);

    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    /**
     * Consul NameResolver Usage.
     *
     *
     * @param serviceName consul service name.
     * @param consulHost consul agent host.
     * @param consulPort consul agent port.
     * @param ignoreConsul if true, consul is not used. instead, the static node list will be used.
     * @param hostPorts the static node list, for instance, Arrays.asList("host1:port1", "host2:port2")
     */
    public HelloWorldClientWithNameResolver(String serviceName,
                                            String consulHost,
                                            int consulPort,
                                            boolean ignoreConsul,
                                            List<String> hostPorts) {

        String consulAddr = "consul://" + consulHost + ":" + consulPort;

        int pauseInSeconds = 5;

        channel = ManagedChannelBuilder
                .forTarget(consulAddr)
                .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
                .nameResolverFactory(new ConsulNameResolver.ConsulNameResolverProvider(serviceName, pauseInSeconds, ignoreConsul, hostPorts))
                .usePlaintext(true)
                .build();

        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }


    public void sayHello() {
        try {
            HelloRequest request = HelloRequest.newBuilder()
                    .setName("grpc load balancer")
                    .build();

            HelloReply response = blockingStub.sayHello(request);
            String message = response.getMessage();
            log.info("message: [{}]", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
