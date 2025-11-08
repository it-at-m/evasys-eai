//
//
// NOTE: This is just a skeleton implementation for illustrative purposes
//
//

// package de.muenchen.evasys.service;

// public class ProcessingService {

//   void processAllItems(List<Item> items) {
//     for (Item item : items) {
//       processItem(item);
//     }
//   }

//   void processItem(Item item) {
//     // Subunit ermitteln
//     Subunit subunit = subunitService.getSubunit(item.getSubunitId());
//     if (subunit == null) {
//       throw new WrongSubunitException();
//     }

//     // Trainer ermitteln und anlegen/aktualisieren
//     User trainer = userService.getUserBySubunit(subunit);
//     if (trainer == null) {
//       userService.insertUser(item.toUser());
//     } else {
//       userService.updateUser(item.toUser());
//     }

//     // Kurs ermitteln und anlegen/aktualisieren
//     Course course = courseService.getCourse(item.getCourseId());
//     if (course == null) {
//       courseService.insertCourse(item.toCourse());
//     } else {
//       courseService.updateCourse(item.toCourse());
//     }
//   }
// }
