package com.controller;

import com.DTO.AuthDTO;
import com.DTO.DetectiveWithoutManIdDTO;
import com.DTO.GenericDTO;
import com.logic.Detective;
import com.security.annotations.IsDetective;
import com.services.interfaces.IAuthorizationService;
import com.services.interfaces.IDetectiveService;
import com.services.interfaces.IHashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthorizationController {
    @Autowired
    private IDetectiveService detectiveService;

    @Autowired
    private IHashService hashService;

    @Autowired
    private IAuthorizationService authorizationService;

    @CrossOrigin
    @RequestMapping(path = "/sign_in", method = RequestMethod.POST)
    public GenericDTO<String> signIn(@RequestBody AuthDTO authData, HttpServletResponse response) {
        Detective detective = detectiveService.getDetectiveByLogin(authData.getLogin());
        if (detective == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new GenericDTO<>(true,"Нет пользователя с таким именем!");
        } else {
            if (hashService.getMD5Hash(authData.getPassword()).equals(detective.getHashOfPassword())) {
                final String token = authorizationService.getToken(detective, 24*7);
                return new GenericDTO<>(false, token);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return new GenericDTO<>(true, "Неправильный пароль!");
            }
        }
    }

    @CrossOrigin
    @RequestMapping(path = "/sign_up", method = RequestMethod.POST)
    public GenericDTO<String> signUp(@RequestBody DetectiveWithoutManIdDTO authData, HttpServletResponse response) {
        boolean exist = detectiveService.existDetectiveWithLogin(authData.getLogin());
        if (exist) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new GenericDTO<>(true, "Пользователь с таким именем уже существует!");
        } else {
            boolean addResult = detectiveService.addDetective(
                    authData.getMan().getName(),
                    authData.getMan().getSurname(),
                    authData.getMan().getBirthday(),
                    authData.getMan().getHomeAddress(),
                    authData.getMan().getPhotoPath(),
                    authData.getLogin(),
                    authData.getPassword(),
                    authData.getEmail()
            );
            if (addResult) {
                return new GenericDTO<>(false, "Вы успешно зарегистрированы в системе!");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return new GenericDTO<>(true, "Не удалось добавить пользователя!");
            }
        }
    }
    /*
    @IsDetective
    @CrossOrigin
    @RequestMapping(path = "/sign_out", method = RequestMethod.DELETE)
    public void signOut(@RequestHeader(value = "deadpool-token") String token) {

    }*/
}
