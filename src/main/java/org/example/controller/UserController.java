package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.dto.UserRequestDto;
import org.example.dto.UserResponseDto;
import org.example.service.UserSpringService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "CRU D операции для управления пользователями")
public class UserController {

    private final UserSpringService userService;

    public UserController(UserSpringService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей со ссылками на детали")
    @ApiResponse(responseCode = "200",
            description = "Список пользователй успешно получен")
    public ResponseEntity<CollectionModel<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        List<UserResponseDto> userWithLinks = users.stream()
                .map(this::addSelfLink)
                .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel();
        Link createLink = linkTo(methodOn(UserController.class).createUser(null)).withRel("create");

        CollectionModel<UserResponseDto> result = CollectionModel.of(userWithLinks, selfLink, createLink);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID",
               description = "Возвращает пользователя по его ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content),
    })
    public ResponseEntity<UserResponseDto> getUserById(
            @Parameter(description = "ID пользователя", required = true) @PathVariable("id") Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(addAllLinks(user));
    }

    @GetMapping("/email")
    @Operation(summary = "Получить пользователя по email",
               description = "Возвращает пользователя по его email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content),
    })
    public ResponseEntity<UserResponseDto> getUserByEmail(
            @Parameter(description = "Email пользователя", required = true)
            @RequestParam String email) {
        UserResponseDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(addAllLinks(user));
    }

    @PostMapping
    @Operation(summary = "Создать пользователя",
               description = "Создание нового пользователя и возврат его с HATEOAS ссылками")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь создан"),
            @ApiResponse(responseCode = "404", description = "Неверные входные данные", content = @Content)
    })
    public ResponseEntity<UserResponseDto> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания пользователя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRequestDto.class))
            )
            @Valid @RequestBody UserRequestDto request) {
        UserResponseDto created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(addAllLinks(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя",
            description = "Обновляет данные существующего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь обновлён"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content),
            @ApiResponse(responseCode = "400", description = "Неверные входные данные", content = @Content)
    })
    public ResponseEntity<UserResponseDto> updateUser(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDto request) {
        UserResponseDto updated = userService.updateUser(id, request);
        return ResponseEntity.ok(addAllLinks(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя",
            description = "Удаляет пользователя по идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь удалён"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponseDto addSelfLink(UserResponseDto dto) {
        dto.add(linkTo(methodOn(UserController.class).getUserById(dto.getId())).withSelfRel());
        return dto;
    }

    private UserResponseDto addAllLinks(UserResponseDto dto) {
        // Ссылка на самого себя
        dto.add(linkTo(methodOn(UserController.class).getUserById(dto.getId())).withSelfRel());

        // Ссылка на список всех пользователей
        dto.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));

        // Ссылка на обновление
        dto.add(linkTo(methodOn(UserController.class).updateUser(dto.getId(), null)).withRel("update"));

        // Ссылка на удаление
        dto.add(linkTo(methodOn(UserController.class)).slash(dto.getId()).withRel("delete"));

        return dto;
    }

}


