package md.ramaiana.foodmarket.domain.storage.presentation.controller;

import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.storage.core.response.StorageResponse;
import md.ramaiana.foodmarket.domain.storage.core.usecase.StorageSearchUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageSearchUseCase searchUseCase;

    @GetMapping
    public List<StorageResponse> findAll() {
        return searchUseCase.findAll();
    }

}
