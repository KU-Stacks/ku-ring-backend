package com.kustacks.kuring.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.kustacks.kuring.domain.category.Category;
import com.kustacks.kuring.domain.category.CategoryRepository;
import com.kustacks.kuring.domain.user.User;
import com.kustacks.kuring.domain.user.UserRepository;
import com.kustacks.kuring.domain.user_category.UserCategory;
import com.kustacks.kuring.domain.user_category.UserCategoryRepository;
import com.kustacks.kuring.event.RollbackEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final FirebaseService firebaseService;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final Map<String, Category> categoryMap;

    public CategoryServiceImpl(
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            UserCategoryRepository userCategoryRepository,
            ApplicationEventPublisher applicationEventPublisher,
            FirebaseService firebaseService) {

        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.userCategoryRepository = userCategoryRepository;

        this.applicationEventPublisher = applicationEventPublisher;

        this.firebaseService = firebaseService;

        categoryMap = categoryRepository.findAllMap();
    }

    @Override
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<String> getCategoryNamesFromCategories(List<Category> categories) {

        return categories.stream()
                .map(Category::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> getUserCategories(String token) {

        User user = userRepository.findByToken(token);
        List<UserCategory> userCategories = userCategoryRepository.findAllByUser(user);

        return userCategories.stream()
                .map(UserCategory::getCategory)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<UserCategory>> compareCategories(List<String> categories, List<UserCategory> dbUserCategories, User user) {

        Map<String, List<UserCategory>> result = new HashMap<>();

        Map<String, UserCategory> dbUserCategoriesMap = listToMap(dbUserCategories);
        Iterator<String> iterator = categories.iterator();

        List<UserCategory> newUserCategories = new LinkedList<>();
        while(iterator.hasNext()) {
            String categoryName = iterator.next();
            UserCategory userCategory = dbUserCategoriesMap.get(categoryName);
            if(userCategory != null) {
                iterator.remove();
                dbUserCategoriesMap.remove(categoryName);
            } else {
                newUserCategories.add(UserCategory.builder()
                        .user(user)
                        .category(categoryMap.get(categoryName))
                        .build());
            }
        }

        result.put("new", newUserCategories);
        result.put("remove", new ArrayList<>(dbUserCategoriesMap.values()));

        return result;
    }
    
    // TODO: FirebaseMessagingException 외에 다른 예외 발생할 수 있는지 확인
    @Transactional
    public void updateUserCategory(String token, Map<String, List<UserCategory>> userCategories) throws FirebaseMessagingException {

        Map<String, List<UserCategory>> transactionHistory = new HashMap<>();
        transactionHistory.put("new", new LinkedList<>());
        transactionHistory.put("remove", new LinkedList<>());

        applicationEventPublisher.publishEvent(new RollbackEvent(token, transactionHistory));

        List<UserCategory> newUserCategories = userCategories.get("new");
        for (UserCategory newUserCategory : newUserCategories) {
            userCategoryRepository.save(newUserCategory);
            firebaseService.subscribe(newUserCategory.getUser().getToken(), newUserCategory.getCategory().getName());
            transactionHistory.get("new").add(newUserCategory);
            log.debug("구독 요청 = {}", newUserCategory.getCategory().getName());
        }

        List<UserCategory> removeUserCategories = userCategories.get("remove");
        for (UserCategory removeUserCategory : removeUserCategories) {
            userCategoryRepository.delete(removeUserCategory);
            firebaseService.unsubscribe(removeUserCategory.getUser().getToken(), removeUserCategory.getCategory().getName());
            transactionHistory.get("remove").add(removeUserCategory);
            log.debug("구독 취소 = {}", removeUserCategory.getCategory().getName());
        }
    }


    private Map<String, UserCategory> listToMap(List<UserCategory> userCategories) {

        Map<String, UserCategory> map = new HashMap<>();
        for (UserCategory userCategory : userCategories) {
            map.put(userCategory.getCategory().getName(), userCategory);
        }

        return map;
    }
}
