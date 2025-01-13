{
    if (cn.sino.devtools.utils.Configs.rpcPrintLog()) {
        try {
            String _uuid = org.slf4j.MDC.get("_rpc_uuid");
            String _serviceName = org.slf4j.MDC.get("_serviceName");
            long _rpc_start = Long.parseLong(org.slf4j.MDC.get("_rpc_start"));
            long _rpc_cost = java.lang.System.currentTimeMillis() - _rpc_start;
            String _responseBody = org.springframework.util.StreamUtils.copyToString($_.body().asInputStream(), java.nio.charset.StandardCharsets.UTF_8);
            _log.info("Rpc [" + _serviceName + " " + _uuid + "] , response body: " + _responseBody);
            _log.info("Rpc [" + _serviceName + " " + _uuid + "] , cost: " + _rpc_cost + "ms");
            $_ = feign.Response.builder()
                .status($_.status())
                .reason($_.reason())
                .headers($_.headers())
                .request($_.request())
                .body(_responseBody.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .build();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}