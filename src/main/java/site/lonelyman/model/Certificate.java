package site.lonelyman.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author LM
 * @since 2022/6/14
 */

@Data
@AllArgsConstructor
public class Certificate {
    private String certificateId;
    private String domain;
    private LocalDateTime certEndTime;
    private boolean isExpiringSoon;
}
