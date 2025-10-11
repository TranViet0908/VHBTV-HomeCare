// src/main/java/Project/HouseService/Controller/Customer/CartController.java
package Project.HouseService.Controller.Customer;

import Project.HouseService.Service.Customer.CartService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/customer/cart")
public class CartController {

    private final CartService cart;
    public CartController(CartService cart){ this.cart = cart; }

    @GetMapping
    public String viewCart() { return "customer/cart/index"; }

    @GetMapping(value = "/json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object cartJson(Authentication auth){
        String u = (auth != null) ? auth.getName() : "guest";
        return cart.getCartJson(u);
    }

    @PostMapping(value = "/items")
    @ResponseBody
    public Object addItem(Authentication auth,
                          @RequestParam Long vendorServiceId,
                          @RequestParam(required = false, defaultValue = "1") Integer quantity,
                          @RequestParam(required = false, name = "scheduleAt") String scheduleAtStr,
                          @RequestParam(required = false) String address,
                          @RequestParam(required = false) String notes){
        LocalDateTime at = parseOptional(scheduleAtStr);
        return cart.addItem(auth.getName(), vendorServiceId, quantity, at, address, notes);
    }

    @PostMapping("/items/{id}/inc")
    @ResponseBody
    public Object inc(Authentication auth, @PathVariable Long id){
        return cart.incItem(auth.getName(), id);
    }

    @PostMapping("/items/{id}/dec")
    @ResponseBody
    public Object dec(Authentication auth, @PathVariable Long id){
        return cart.decItem(auth.getName(), id);
    }

    @DeleteMapping("/items/{id}")
    @ResponseBody
    public Object remove(Authentication auth, @PathVariable Long id){
        return cart.removeItem(auth.getName(), id);
    }

    @PostMapping("/apply-coupon")
    @ResponseBody
    public Object applyCoupon(Authentication auth, @RequestParam String code){
        return cart.applyCoupon(auth.getName(), code.trim());
    }

    @DeleteMapping("/coupon")
    @ResponseBody
    public Object removeCoupon(Authentication auth){
        return cart.removeCoupon(auth.getName());
    }

    private static LocalDateTime parseOptional(String s){
        if (s == null || s.isBlank()) return null;
        try { return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME); }
        catch (Exception e){ return null; }
    }
}
