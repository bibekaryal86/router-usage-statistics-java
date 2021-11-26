package router.usage.statistics.java.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ModelResponse {
    private List<Model> modelList;
    private Set<String> yearMonthSet;
    private Model modelTotal;

    // no args constructor
    public ModelResponse() {
        super();
    }

    // all args constructor
    public ModelResponse(List<Model> modelList, Set<String> yearMonthSet, Model modelTotal) {
        this.modelList = modelList;
        this.yearMonthSet = yearMonthSet;
        this.modelTotal = modelTotal;
    }

    // getters
    public List<Model> getModelList() {
        return modelList;
    }

    public Set<String> getYearMonthSet() {
        return yearMonthSet;
    }

    public Model getModelTotal() {
        return modelTotal;
    }

    // setters with builder pattern
    public ModelResponse setModelList(List<Model> modelList) {
        this.modelList = modelList;
        return this;
    }

    public ModelResponse setYearMonthSet(Set<String> yearMonthSet) {
        this.yearMonthSet = yearMonthSet;
        return this;
    }

    public ModelResponse setModelTotal(Model modelTotal) {
        this.modelTotal = modelTotal;
        return this;
    }

    @Override
    public String toString() {
        return "ModelResponse {" +
                "modelList=" + modelList +
                ", yearMonthSet=" + yearMonthSet +
                ", modelTotal=" + modelTotal +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ModelResponse model = (ModelResponse) o;
        return Objects.equals(modelList, model.modelList) &&
                Objects.equals(yearMonthSet, model.yearMonthSet) &&
                Objects.equals(modelTotal, model.modelTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelList, yearMonthSet, modelTotal);
    }
}
