package zatribune.spring.ex_mongodb_docker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private String id;

    @NotBlank
    @Size(min = 5, max = 255)
    private String description;

    @Min(0)
    @Max(5000)
    private BigDecimal price;

    @URL
    private String imageUrl;
}
