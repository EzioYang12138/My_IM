package cn.yangjian.im.common.routealgorithm.consistenthash;

import cn.yangjian.im.common.routealgorithm.RouteHandle;

import java.util.List;

public class ConsistentHashHandle implements RouteHandle {
    private AbstractConsistentHash hash ;

    public void setHash(AbstractConsistentHash hash) {
        this.hash = hash;
    }

    @Override
    public String routeServer(List<String> values, String key) {
        return hash.process(values, key);
    }
}
