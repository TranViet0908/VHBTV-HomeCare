// src/main/java/Project/HouseService/Controller/Vendor/VendorLayoutAdvice.java
package Project.HouseService.Controller.Vendor;

import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Repository.VendorProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice(annotations = Controller.class)
public class VendorLayoutAdvice {
    private final VendorProfileRepository profiles;

    public VendorLayoutAdvice(VendorProfileRepository profiles) {
        this.profiles = profiles;
    }

    @ModelAttribute("vendorProfile")
    public VendorProfile vendorProfile(Authentication auth) {
        if (auth == null) return null;
        Optional<VendorProfile> vp = profiles.findByUser_Username(auth.getName());
        return vp.orElse(null);
    }
}
