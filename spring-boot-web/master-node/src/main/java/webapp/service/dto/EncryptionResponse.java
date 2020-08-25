package webapp.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EncryptionResponse {
  private final String id;
  private final String result;
}