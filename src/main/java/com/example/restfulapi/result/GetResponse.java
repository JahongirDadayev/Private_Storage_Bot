package com.example.restfulapi.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetResponse<T> {
    private SendMessage sendMessage;
    private List<T> objects;
}
