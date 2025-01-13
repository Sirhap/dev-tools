{
    org.slf4j.MDC.put("_rpc_uuid", java.util.UUID.randomUUID().toString().substring(0, 8));
    org.slf4j.MDC.put("_rpc_start", java.lang.System.currentTimeMillis() + "");
    if (cn.sino.devtools.utils.Configs.rpcPrintLog()) {
        String _uuid = org.slf4j.MDC.get("_rpc_uuid");
        String _serviceName = org.slf4j.MDC.get("_serviceName");
//        String _url = request.url();
//        String _body  = String.value(request.body());
        String requestToString = request.toString();
//        _log.info("Rpc [{}] , request url: {}", _serviceName + " " + _uuid, _url);
//        _log.info("Rpc [{}] , request body: {}", _serviceName + " " + _uuid, _body);
        _log.info("Rpc [{}] , request: {}", _serviceName + " " + _uuid, requestToString);
    }
}