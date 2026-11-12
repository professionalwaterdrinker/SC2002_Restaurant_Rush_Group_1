package restaurantrush.model.staff;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.model.Restaurant;

/** Stage 3: seats at most one queued customer per turn, in addition to the Waiter's task. */
public class Host extends SeatingStaff {

    public Host(String name) {
        super(name);
    }

    @Override
    public String getRole() {
        return "Host";
    }

    @Override
    public List<String> availableTasks(Restaurant restaurant) {
        List<String> tasks = new ArrayList<>();
        String seating = seatingTask(restaurant);
        if (seating != null) {
            tasks.add(seating);
        }
        return tasks;
    }
}
