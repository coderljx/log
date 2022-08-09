package com.example.Pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Model {

 private String field;
 private String label;
 private String operator;
 private String type;
 private String datatype;
 private String canInput;
 private List<label> otions;


 @Data
 public static class label {
  private String label;
  private String value;
 }

}
