package klu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/backend/items")

@CrossOrigin(origins = "http://localhost:5173")
public class ItemController {

    @Autowired
    private ItemRepo itemRepository;

    private static final String IMAGE_DIR = "images/";

    @PostMapping("/items")
    public ResponseEntity<Items> addItem(
        @RequestParam int pid,
        @RequestParam String pname,
        @RequestParam float pprs,
        @RequestParam String pcategory,
        @RequestParam int quantity,
        @RequestParam("image") MultipartFile imageFile
    ) {
        try {
            // Ensure images directory exists
            File dir = new File(IMAGE_DIR);
            if (!dir.exists()) dir.mkdirs();

            // Save file to disk
            String imageName = imageFile.getOriginalFilename();
            Path imagePath = Paths.get(IMAGE_DIR + imageName);
            Files.write(imagePath, imageFile.getBytes());

            // Save item to DB
            Items item = new Items();
            item.setPid(pid);
            item.setPname(pname);
            item.setPprs(pprs);
            item.setPcategory(pcategory);
            item.setQuantity(quantity);
            item.setPimg(imageName); // Only store file name, not full path

            Items savedItem = itemRepository.save(item);
            return ResponseEntity.ok(savedItem);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/items")
    public ResponseEntity<?> getAllItems() {
        return ResponseEntity.ok(itemRepository.findAll());
    }

    @DeleteMapping("/items/{pid}")
    public ResponseEntity<Void> deleteItem(@PathVariable int pid) {
        if (itemRepository.existsById(pid)) {
            itemRepository.deleteById(pid);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
