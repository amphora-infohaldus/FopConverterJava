package domain;

import enums.FileType;
import lombok.Getter;
import lombok.Setter;

import java.util.Base64;

@Getter
@Setter
public class ConversionRequest {

    private final FileType[] fileTypes = FileType.values();

    public String Data;
    public int From;
    public int To;
    public String FromExtension;
    public String ToExtension;

    public ConversionRequest() {}
    public ConversionRequest(String Data, int From, int To, String FromExtension, String ToExtension) {
        this.Data = Data;
        this.From = From;
        this.To = To;
        this.FromExtension = FromExtension;
        this.ToExtension = ToExtension;
    }

    public FileType getFrom() {
        return fileTypes[From - 1];
    }
    public FileType getTo() {
        return fileTypes[To - 1];
    }
    public byte[] getDataBytes() {
        return Base64.getDecoder().decode(Data);
    }
}
