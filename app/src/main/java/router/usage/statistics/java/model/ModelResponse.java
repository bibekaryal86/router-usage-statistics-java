package router.usage.statistics.java.model;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelResponse {
  private List<Model> modelList;
  private Set<String> yearMonthSet;
  private Model modelTotal;
}
