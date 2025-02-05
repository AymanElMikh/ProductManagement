/*
 * Copyright (c) 2025.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */

package labs.pm.app;

import labs.pm.data.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Predicate;


/**
 * {@code Shop} class represents an application that manages Products
 * @version 1.0
 * @author AymanElMikh
 */
public class Shop {
    public static void main(String[] args) {

        ProductManager productManager =  new ProductManager("en-GB");
        // Create
        productManager.createProduct(1, "Bob", BigDecimal.valueOf(123.22), Rating.NOT_RATED, LocalDate.now().plusMonths(3).plusDays(2));
        productManager.createProduct(2, "Marlie", BigDecimal.valueOf(1.22), Rating.NOT_RATED);
        // Review
        productManager.reviewProduct(1, Rating.FOUR_STAR, "This is delicious food");
        productManager.reviewProduct(1, Rating.TWO_STAR , "Hmmm delicious");
        productManager.reviewProduct(39, Rating.FIVE_STAR, "This cannot be cooked two time, it's magic");
        productManager.reviewProduct(2, Rating.THREE_STAR , "Thank you so mush");
        productManager.parseReview("1, 5, This is too delicious");
        productManager.parseProduct("F, 4, Shoes, 31.213, 5, 2000-07-22");

        productManager.printProductReport(1);
    }
}