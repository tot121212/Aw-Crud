package com.crud_project.crud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.crud_project.crud.entity.PageState;
import com.crud_project.crud.entity.WheelSpinResult;
import com.crud_project.crud.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/crud")
@RequiredArgsConstructor
@Slf4j
public class CrudController {
    private final UserService userService;

    @GetMapping("/.well-known/**")
    @ResponseBody
    public ResponseEntity<Void> handleWellKnown() {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("") //"/crud"
    public String getCrud(
        Model model,
        @SessionAttribute(SessionKeys.CUR_USER_NAME) String name, 
        @SessionAttribute(SessionKeys.CUR_USER_PAGE_STATE) PageState pageState
    ) {
        //log.info(String.valueOf(userService.getUserProjectionByName(name).isDead()));
        model.addAttribute(
            ModelKeys.CUR_USER_PROJECTION, 
            userService.getUserProjectionByName(name));
        model.addAttribute(
            ModelKeys.REQ_PAGE_PROJECTIONS, 
            userService.getUserProjectionsByPageState(pageState));
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
        return "redirect:/logout";
    }

    // @PostMapping("/create")
    // public String createPost(
    //     @RequestParam String username, 
    //     @RequestParam String password
    // ) {
    //     boolean isUsernameValid = StateValidationUtils.isValidUsername(username);
    //     boolean isPasswordValid = StateValidationUtils.isValidPassword(password);
    //     String registreeUsername = isUsernameValid ? username : null;
    //     boolean isRegistered = false;
    //     String fragment = "user-create-form";

    //     if (isUsernameValid 
    //         && isPasswordValid 
    //         && (userService.registerUser(username, password) != null)){
    //         isRegistered = true;
    //     }

    //     return "redirect:" + 
    //     UriComponentsBuilder
    //         .fromPath("/crud")
    //         .queryParam("registerUser", isRegistered)
    //         .queryParam("isUsernameValid", isUsernameValid)
    //         .queryParam("isPasswordValid", isPasswordValid)
    //         .queryParam("registerUserUsername", registreeUsername)
    //         .fragment(fragment)
    //         .build()
    //         .toUriString();
    // }

    @PostMapping("/requestPage")
    public String requestPagePost(
        HttpSession session, 
        @RequestParam(defaultValue = "0") Integer pageNumber, 
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        if (PageState.isValidPage(pageNumber) && 
            PageState.isValidSize(pageSize))
        {
            session.setAttribute(
                SessionKeys.CUR_USER_PAGE_STATE, 
                PageState
                    .builder()
                    .page(pageNumber)
                    .size(pageSize)
                    .build()
            );
            return "redirect:/crud" + "#user-table";
        }

        return "redirect:/crud" + "?userTableError=true" + "#request-page-form-container";
    }

    @PostMapping("/spinWheel")
    public String spinWheelPost(
        Authentication authentication,
        RedirectAttributes redirectAttributes,
        @SessionAttribute(SessionKeys.CUR_USER_PAGE_STATE) PageState pageState
    ) {
        WheelSpinResult wheelSpinResult = userService.spinWheel(authentication.getName(), pageState);
        redirectAttributes.addFlashAttribute(ModelKeys.WHEEL_WINNER, wheelSpinResult.getWinnerName());
        redirectAttributes.addFlashAttribute(ModelKeys.WHEEL_PARTICIPANTS, wheelSpinResult.getParticipants());
        return "redirect:/crud" + "#user-wheel";
    }
}
