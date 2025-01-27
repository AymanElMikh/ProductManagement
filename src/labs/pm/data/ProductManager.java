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

package labs.pm.data;

import java.awt.dnd.DropTarget;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author AymanElMikh
 **/
public class ProductManager {



    private Product product;
    private Review review;
    private final ResourceBundle resource;
    private final DateTimeFormatter dateFormat;
    private final NumberFormat moneyFormat;

    public ProductManager(Locale locale){

        resource = ResourceBundle.getBundle("labs.pm.data.resources", locale);
        dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
        moneyFormat = NumberFormat.getCurrencyInstance(locale);

    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore){

        product = new Food(id, name, price, rating, bestBefore);

        return product;
    }

    public  Product createProduct(int id, String name, BigDecimal price, Rating rating){

        product = new Drink(id, name, price, rating);
        return product;

    }

    public Product reviewProduct(Product product, String comment,Rating rating){

        review = new Review(rating, comment);
        this.product = product.applyRating(review.rating());
        return product;
    }


    public void printProductReport(){

        StringBuilder txt = new StringBuilder();

        String type = switch (product){
            case Food food -> resource.getString("food");
            case Drink drink -> resource.getString("drink");
        };

        txt.append(MessageFormat.format(resource.getString("product"), product.getName(), moneyFormat.format(product.getPrice()),
                product.getRating().getStars(), dateFormat.format(product.getBestBefore()),type));

        txt.append("\n");
        if( review != null ){
            txt.append(MessageFormat.format(resource.getString("review"),
                    review.rating().getStars(), review.comments()));
        } else {
            txt.append(resource.getString("no.reviews"));
        }

        txt.append("\n");
        System.out.println(txt);

    }

}