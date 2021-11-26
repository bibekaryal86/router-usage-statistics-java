package router.usage.statistics.java.model;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.Objects;

public class Model {
    @BsonId
    private ObjectId id;
    private String date;
    private String year;
    private String day;
    @BsonProperty(value = "data_upload")
    private String dataUpload;
    @BsonProperty(value = "data_download")
    private String dataDownload;
    @BsonProperty(value = "data_total")
    private String dataTotal;

    // no args constructor
    public Model() {
        super();
    }

    // all args constructor
    public Model(ObjectId id, String date, String year, String day, String dataUpload, String dataDownload, String dataTotal) {
        this.id = id;
        this.date = date;
        this.year = year;
        this.day = day;
        this.dataUpload = dataUpload;
        this.dataDownload = dataDownload;
        this.dataTotal = dataTotal;
    }

    // getters
    public ObjectId getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getYear() {
        return year;
    }

    public String getDay() {
        return day;
    }

    public String getDataUpload() {
        return dataUpload;
    }

    public String getDataDownload() {
        return dataDownload;
    }

    public String getDataTotal() {
        return dataTotal;
    }

    // setters with builder pattern
    public Model setId(ObjectId id) {
        this.id = id;
        return this;
    }

    public Model setDate(String date) {
        this.date = date;
        return this;
    }

    public Model setYear(String year) {
        this.year = year;
        return this;
    }

    public Model setDay(String day) {
        this.day = day;
        return this;
    }

    public Model setDataUpload(String dataUpload) {
        this.dataUpload = dataUpload;
        return this;
    }

    public Model setDataDownload(String dataDownload) {
        this.dataDownload = dataDownload;
        return this;
    }

    public Model setDataTotal(String dataTotal) {
        this.dataTotal = dataTotal;
        return this;
    }

    @Override
    public String toString() {
        return "Model {" +
                "id=" + id +
                ", date=" + date +
                ", year=" + year +
                ", day=" + day +
                ", data_upload=" + dataUpload +
                ", data_download=" + dataDownload +
                ", data_total=" + dataTotal +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Model model = (Model) o;
        return Objects.equals(id, model.id) &&
                Objects.equals(date, model.date) &&
                Objects.equals(year, model.year) &&
                Objects.equals(day, model.day) &&
                Objects.equals(dataUpload, model.dataUpload) &&
                Objects.equals(dataDownload, model.dataDownload) &&
                Objects.equals(dataTotal, model.dataTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, year, day, dataUpload, dataDownload, dataTotal);
    }
}
