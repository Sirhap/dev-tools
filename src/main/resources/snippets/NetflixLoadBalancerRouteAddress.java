{
    try{
        com.netflix.loadbalancer.Server server = null;
        String _serviceName = this$0.loadBalancerContext.getClientName().toLowerCase();
        String _addr = cn.sino.devtools.utils.Configs.getServiceAndAddrMap().get(_serviceName);
        String _globalMapping = cn.sino.devtools.utils.Configs.getGlobalMapping();

        boolean _configured_addr = _addr != null && _addr.length() > 0;
        if (!_configured_addr && _globalMapping != null && _globalMapping.length() > 0) {
            _addr = _globalMapping;
        }
        org.slf4j.MDC.put("_serviceName", _serviceName);

        if(_addr != null && _addr.length() > 0){
            if (_addr.startsWith("@")) {
                String _env = _addr.substring(1).toLowerCase();
                System.out.println("_env: " + _env);
                java.util.Map _envAndEurekaServerMap = cn.sino.devtools.utils.Configs.getEnvAndEurekaServerMap();
                String eurekaServer = _envAndEurekaServerMap.get(_env);
                java.util.Map _serviceAndInstanceMap = cn.sino.devtools.utils.EurekaUtils.getServiceAndInstanceMap(eurekaServer);
                System.out.println("_serviceAndInstanceMap: " + _serviceAndInstanceMap);
                String _instance = _serviceAndInstanceMap.get(_serviceName);
                System.out.println("Rpc serviceName: " + _serviceName + ", server: " +  _instance);
                server = new com.netflix.loadbalancer.Server(_instance);
            } else {
                server = new com.netflix.loadbalancer.Server(_addr);
            }
        } else {
            server = this$0.loadBalancerContext.getServerFromLoadBalancer(this$0.loadBalancerURI,this$0.loadBalancerKey);
        }
        System.out.println("Rpc serviceName: " + _serviceName + ", server: " +  server);
        $1.onNext(server);
        $1.onCompleted();
    }catch(Exception e){
    }
}