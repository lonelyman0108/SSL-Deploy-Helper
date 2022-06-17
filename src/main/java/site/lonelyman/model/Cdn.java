package site.lonelyman.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <p>
 * CDN实体类
 * </p>
 *
 * @author LM
 * @since 2022/6/17
 */

@Data
@AllArgsConstructor
public class Cdn {
    private String domain;
    private boolean isHttps;
}
