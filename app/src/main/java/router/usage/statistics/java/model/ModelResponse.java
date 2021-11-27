package router.usage.statistics.java.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelResponse {
    private List<Model> modelList;
    private Set<String> yearMonthSet;
    private Model modelTotal;
}
