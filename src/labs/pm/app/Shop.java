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
import java.util.Locale;


/**
 * {@code Shop} class represents an application that manages Products
 * @version 1.0
 * @author AymanElMikh
 */
public class Shop {
    public static void main(String[] args) {

        ProductManager productManager =  new ProductManager("en-GB");

        Product p1 = productManager.createProduct(1, "Bob", BigDecimal.valueOf(123.22), Rating.NOT_RATED, LocalDate.now().plusMonths(3).plusDays(2));


        productManager.reviewProduct(1, "This is delicious food", Rating.FOUR_STAR);
        productManager.reviewProduct(1, "Hmmm delicious", Rating.THREE_STAR);
        productManager.reviewProduct(1, "This cannot be cooked two time, it's magic", Rating.FIVE_STAR);
        productManager.reviewProduct(1, "Thank you so mush", Rating.TWO_STAR);

        productManager.printProductReport(1);

        productManager.changeLocal("ru-RU");

        productManager.printProductReport(1);

    }




}