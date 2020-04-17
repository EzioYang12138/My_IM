package cn.yangjian.im.route.vo.res;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class IMServerResVO implements Serializable {

    private String ip ;
    private Integer imServerPort;
    private Integer httpPort;
}
