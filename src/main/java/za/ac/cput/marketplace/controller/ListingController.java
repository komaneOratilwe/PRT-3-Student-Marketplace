package za.ac.cput.marketplace.controller;

import za.ac.cput.marketplace.domain.Listing;
import za.ac.cput.marketplace.domain.User;
import za.ac.cput.marketplace.repository.UserRepository;
import za.ac.cput.marketplace.service.ListingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@CrossOrigin(origins = "*")
public class ListingController {

    @Autowired
    private ListingService listingService;

    @Autowired
    private UserRepository userRepository;

    // Anyone can browse approved listings — no login needed
    @GetMapping
    public List<Listing> getApprovedListings() {
        return listingService.getApprovedListings();
    }

    // Only someone logged in as SELLER can create a listing
    @PostMapping
    public Listing createListing(@RequestBody Listing listing, HttpServletRequest request) {
        requireLoginAs(request, "SELLER");

        Long sellerId = Long.valueOf(request.getAttribute("userId").toString());
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        return listingService.createListing(listing, seller);
    }

    // A seller views their own listings — must be logged in as SELLER
    @GetMapping("/seller/{sellerId}")
    public List<Listing> getListingsBySeller(@PathVariable Long sellerId, HttpServletRequest request) {
        requireLoginAs(request, "SELLER");
        return listingService.getListingsBySeller(sellerId);
    }

    // Admin only: view listings awaiting approval
    @GetMapping("/pending")
    public List<Listing> getPendingListings(HttpServletRequest request) {
        requireRole(request, "ADMIN");
        return listingService.getPendingListings();
    }

    // Admin only: approve a listing
    @PutMapping("/{id}/approve")
    public Listing approveListing(@PathVariable Long id, HttpServletRequest request) {
        requireRole(request, "ADMIN");
        return listingService.approveListing(id);
    }

    // Admin only: reject a listing
    @PutMapping("/{id}/reject")
    public Listing rejectListing(@PathVariable Long id, HttpServletRequest request) {
        requireRole(request, "ADMIN");
        return listingService.rejectListing(id);
    }

    // --- Helper checks ---

    private void requireRole(HttpServletRequest request, String requiredRole) {
        Object role = request.getAttribute("role");
        if (role == null) {
            throw new RuntimeException("Not logged in");
        }
        if (!requiredRole.equalsIgnoreCase(role.toString())) {
            throw new RuntimeException("Access denied: requires " + requiredRole + " role");
        }
    }

    private void requireLoginAs(HttpServletRequest request, String requiredMode) {
        Object loginAs = request.getAttribute("loginAs");
        if (loginAs == null) {
            throw new RuntimeException("Not logged in");
        }
        if (!requiredMode.equalsIgnoreCase(loginAs.toString())) {
            throw new RuntimeException("Access denied: you must be logged in as " + requiredMode);
        }
    }
}