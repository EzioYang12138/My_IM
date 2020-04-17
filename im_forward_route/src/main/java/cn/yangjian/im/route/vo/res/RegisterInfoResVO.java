package cn.yangjian.im.route.vo.res;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class RegisterInfoResVO implements Serializable {
    private Long userId ;
    private String userName ;
}
