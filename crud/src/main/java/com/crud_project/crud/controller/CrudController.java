package com.crud_project.crud.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.crud_project.crud.service.AuthService;
import com.crud_project.crud.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/crud")
@RequiredArgsConstructor
@Slf4j
public class CrudController {
    private final UserService userService;
    private final AuthService authService;

    @GetMapping("") //"/crud"
    public String getCrud(
            Model model,
            Authentication authentication,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String wheelWinner) {
        model.addAttribute(
            "isDeleted",
            userService.getIsDeletedByName(authentication.getName()));
        model.addAttribute(
            "currentUser",
            userService.getUserProjectionByName(authentication.getName()));
        model.addAttribute(
            "userPage", 
            userService.getUserProjectionsByPageAndSize(pageNumber, pageSize));
        model.addAttribute(
            "userPageFilter", 
            filter);
        if (wheelWinner != null) {
            model.addAttribute("wheelWinner", wheelWinner);
        }
        return "crud";
    }

    // interact with and filter requests here
    @PostMapping("/create-test-users")
    public String createTestUsersPost() {
        userService.createTestUsers();
        return "redirect:/crud";
    }

    @PostMapping("/delete-test-users")
    public String deleteTestUsersPost() {
        userService.deleteTestUsers();
        return "redirect:/crud";
    }

    @PostMapping("/delete-all-users")
    public String deleteAllUsersPost(HttpServletRequest request) {
        userService.deleteAllUsers();
        authService.logout(request);
        return "redirect:/home";
    }

    // TODO: add create, read, update, delete, and awCrud functionality
    // Probably attach all this to the userTable
    @PostMapping("/create")
    public String createPost(
            @RequestParam String username, 
            @RequestParam String password) {
        return "redirect:/crud" + 
        "?registerUser=" + authService.registerUser(username, password) + 
        "&registerUserUsername=" + username;
    }

    @PostMapping("/requestPage")
    public String requestPagePost(
            Model model,
            @RequestParam(defaultValue = "0") Integer pageNumber, 
            @RequestParam(defaultValue = "10") Integer pageSize, 
            @RequestParam(required = false) String filter){
        return "redirect:/crud" + 
        "?pageNumber=" + pageNumber + 
        "&pageSize=" + pageSize + 
        "&filter=" + filter + 
        "#request-page-form";
    }

    @PostMapping("/spinWheel")
    public String spinWheelPost(
            Model model, 
            Authentication authentication,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String isDeletedUser = userService.spinWheel(model, authentication.getName(), pageNumber, pageSize);
        // add wheel data to the session since its hard to persist everything with uri

        // js script will be within the fragment that will use a
        // function that takes uri param named wheelLoser and 
        // just calculate the velocity needed 
        // to propel the wheel as such that the friction causes it to 
        // slow perfectly to a specified position

        return "redirect:/crud" + 
        "?wheelWinner=" + isDeletedUser + 
        "&pageNumber=" + pageNumber + 
        "&pageSize=" + pageSize + 
        "#user-wheel";
    }
}
