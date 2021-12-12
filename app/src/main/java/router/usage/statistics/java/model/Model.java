package router.usage.statistics.java.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
