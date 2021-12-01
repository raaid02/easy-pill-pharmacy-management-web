/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import entities.Medicine;
import entities.Supplier;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import model.MedicineFacade;
import model.SupplierFacade;
import utilities.MedicineIdGenerator;

@MultipartConfig(fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 100,
        location = "D:\\Documents\\NetBeansProjects\\easy-pill\\easy-pill-war\\web\\ProductImages\\"
)

/**
 *
 * @author Topsy
 */
public class MedicineController extends HttpServlet {

    @EJB
    private MedicineIdGenerator medicineIdGenerator;

    @EJB
    private SupplierFacade supplierFacade;

    @EJB
    private MedicineFacade medicineFacade;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String command = request.getParameter("command");
        if (command == null) {
            command = "MEDICINE";
        }
        switch (command) {
            case "MEDICINE":
                redirectAdminMedicine(request, response);
                break;
            case "MEDICINEOPTION":
                redirectMedicineOptionSelection(request, response);
                break;
            case "ADDMEDICINE":
                redirectAddMedicine(request, response);
                break;

        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String command = request.getParameter("command");
            if (command == null) {
                command = "MEDICINE";
            }
            switch (command) {
                case "MEDICINE":
                    redirectAdminMedicine(request, response);
                    break;
                case "CONFIRMADDMEDICINE":
                    addMedicine(request, response);
                    break;
                case "DELETEMEDICINE":
                    deleteMedicine(request, response);
                    break;
                case "UPDATEMEDICINE":
                    confirmUpdateMedicine(request, response);
                    break;

            }
        } catch (SQLException ex) {
            Logger.getLogger(MedicineController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void redirectAdminMedicine(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        request.setAttribute("username", (String) session.getAttribute("UserFirstName") + " " + (String) session.getAttribute("UserLastName"));
        List<Medicine> instockList = medicineFacade.getAllMedicineInStock();
        List<Medicine> outstockList = medicineFacade.getAllMedicineOutOfStock();
        request.setAttribute("INSTOCKLIST", instockList);
        request.setAttribute("OUTSTOCKLIST", outstockList);
        request.setAttribute("inStockCount", instockList.size());
        request.setAttribute("outOfStockCount", outstockList.size());
        request.setAttribute("medicineCount", (instockList.size() + outstockList.size()));
        RequestDispatcher dispatcher = request.getRequestDispatcher("/adminMedicinePage.jsp");
        dispatcher.forward(request, response);

    }

    private void deleteMedicine(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String medicineId = request.getParameter("medicineId");
        Medicine medicine = medicineFacade.getMedicineById(medicineId);
        Path imagePath = FileSystems.getDefault().getPath("D:\\Documents\\NetBeansProjects\\easy-pill\\easy-pill-war\\web\\" + medicine.getImagePath());
        Files.deleteIfExists(imagePath);
        medicineFacade.removeMedicine(medicineId);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/deleteMedicineSuccess.jsp");
        dispatcher.forward(request, response);
    }

    private void addMedicine(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String imagePath;
        String name = request.getParameter("name");
        String supplier = request.getParameter("supplier");
        String metric = request.getParameter("metric");
        String description = request.getParameter("description");
        String price = request.getParameter("price");
        String weight = request.getParameter("weight");
        String quantity = request.getParameter("quantity");
        Part imagePart = request.getPart("image");
        //Validating  Photo and File Uploads
        if (!imagePart.getContentType().equalsIgnoreCase("image/jpeg")) {
            if (!imagePart.getContentType().equalsIgnoreCase("image/png")) {
                request.setAttribute("errorMessage", "Please make sure an image is uploaded here!");
                request.setAttribute("name", request.getParameter("name"));
                request.setAttribute("supplier", request.getParameter("isbn"));
                request.setAttribute("metric", request.getParameter("metric"));
                request.setAttribute("description", request.getParameter("description"));
                request.setAttribute("price", request.getParameter("price"));
                request.setAttribute("quantity", request.getParameter("quantity"));
                request.setAttribute("weight", request.getParameter("weight"));
                RequestDispatcher dispatcher = request.getRequestDispatcher("/adminAddMedicinePage.jsp");
                dispatcher.forward(request, response);
            }
        } else {
            String medicineId = medicineIdGenerator.generateNumber();
            if (imagePart.getContentType().equalsIgnoreCase("image/jpeg")) {
                imagePart.write(medicineId + ".jpg");
                imagePath = "ProductImages\\" + medicineId + ".jpg";
            } else {
                imagePart.write(medicineId + ".png");
                imagePath = "ProductImages\\" + medicineId + ".png";
            }
            Medicine medicine = new Medicine();
            medicine.setMedicineId(medicineId);
            medicine.setName(name);
            medicine.setDescription(description);
            medicine.setImagePath(imagePath);
            medicine.setSupplierId(supplier);
            medicine.setPrice(price);
            medicine.setWeight(weight + " " + metric);
            medicine.setQuantity(quantity);
            medicineFacade.create(medicine);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/adminUpdateMedicineSuccess.jsp");
            dispatcher.forward(request, response);

        }
    }

    private void confirmUpdateMedicine(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, SQLException {
        String option = request.getParameter("option");
        String medicineId;
        switch (option) {
            case "image":
                Part imagePart = request.getPart("image");
                medicineId = request.getParameter("medicineId");
                if (!imagePart.getContentType().equalsIgnoreCase("image/jpeg")) {
                    if (!imagePart.getContentType().equalsIgnoreCase("image/png")) {
                        request.setAttribute("uploadError", "Uploaded file was not an image. Please re-upload an image.");
                        request.setAttribute("name", request.getParameter("name"));
                        request.setAttribute("medicineId", request.getParameter("medicineId"));
                        RequestDispatcher dispatcher = request.getRequestDispatcher("/adminUpdateMedicineImagePage.jsp");
                        dispatcher.forward(request, response);
                    }
                }
                if (imagePart.getContentType().equalsIgnoreCase("image/jpeg")) {
                    imagePart.write(medicineId + ".jpg");
                } else if (imagePart.getContentType().equalsIgnoreCase("image/png")) {
                    imagePart.write(medicineId + ".png");
                }
                RequestDispatcher dispatcher = request.getRequestDispatcher("/adminUpdateMedicineSuccess.jsp");
                dispatcher.forward(request, response);
                break;

            case "name":
                medicineId = request.getParameter("medicineId");
                String newName = request.getParameter("newname");
                medicineFacade.updateName(medicineId, newName);
                RequestDispatcher dispatcher2 = request.getRequestDispatcher("/adminUpdateMedicineSuccess.jsp");
                dispatcher2.forward(request, response);
                break;
            case "description":
                medicineId = request.getParameter("medicineId");
                String newDesc = request.getParameter("newDesc");
                medicineFacade.updateDescription(medicineId, newDesc);
                RequestDispatcher dispatcher3 = request.getRequestDispatcher("/adminUpdateMedicineSuccess.jsp");
                dispatcher3.forward(request, response);
                break;
            case "price":
                medicineId = request.getParameter("medicineId");
                String newPrice = request.getParameter("newPrice");
                medicineFacade.updatePrice(medicineId, newPrice);
                RequestDispatcher dispatcher4 = request.getRequestDispatcher("/adminUpdateMedicineSuccess.jsp");
                dispatcher4.forward(request, response);
                break;
            case "quantity":
                medicineId = request.getParameter("medicineId");
                String newQuantity = request.getParameter("newQuantity");
                medicineFacade.updateQuantity(medicineId, newQuantity);
                RequestDispatcher dispatcher5 = request.getRequestDispatcher("/adminUpdateMedicineSuccess.jsp");
                dispatcher5.forward(request, response);
                break;

            default:
                break;
        }

    }

    private void redirectMedicineOptionSelection(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String option = request.getParameter("option");
        switch (option) {
            case "image":
                redirectUpdateMedicineImage(request, response);
                break;
            case "name":
                redirectUpdateMedicineName(request, response);
                break;
            case "description":
                redirectUpdateMedicineDescription(request, response);
                break;
            case "price":
                redirectUpdateMedicinePrice(request, response);
                break;
            case "quantity":
                redirectUpdateMedicineQuantity(request, response);
                break;
            case "delete":
                redirectDeleteMedicine(request, response);
                break;
            default:
                break;
        }
    }

    private void redirectUpdateMedicineImage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String medicineId = request.getParameter("medicineId");
        Medicine medicine = medicineFacade.getMedicineById(medicineId);
        request.setAttribute("name", medicine.getName());
        request.setAttribute("medicineId", medicineId);
        request.setAttribute("imagePath", medicine.getImagePath());
        RequestDispatcher dispatcher = request.getRequestDispatcher("/adminUpdateMedicineImagePage.jsp");
        dispatcher.forward(request, response);
    }

    private void redirectUpdateMedicineName(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String medicineId = request.getParameter("medicineId");
        Medicine medicine = medicineFacade.getMedicineById(medicineId);
        request.setAttribute("name", medicine.getName());
        request.setAttribute("medicineId", medicineId);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/adminUpdateMedicineNamePage.jsp");
        dispatcher.forward(request, response);
    }

    private void redirectUpdateMedicineDescription(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String medicineId = request.getParameter("medicineId");
        Medicine medicine = medicineFacade.getMedicineById(medicineId);
        request.setAttribute("description", medicine.getDescription());
        request.setAttribute("medicineId", medicineId);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/adminUpdateMedicineDescriptionPage.jsp");
        dispatcher.forward(request, response);
    }

    private void redirectUpdateMedicinePrice(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String medicineId = request.getParameter("medicineId");
        Medicine medicine = medicineFacade.getMedicineById(medicineId);
        request.setAttribute("price", medicine.getPrice());
        request.setAttribute("medicineId", medicineId);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/adminUpdateMedicinePricePage.jsp");
        dispatcher.forward(request, response);
    }

    private void redirectUpdateMedicineQuantity(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String medicineId = request.getParameter("medicineId");
        Medicine medicine = medicineFacade.getMedicineById(medicineId);
        request.setAttribute("quantity", medicine.getQuantity());
        request.setAttribute("medicineId", medicineId);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/adminUpdateMedicineQuantityPage.jsp");
        dispatcher.forward(request, response);
    }

    private void redirectDeleteMedicine(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String medicineId = request.getParameter("medicineId");
        Medicine medicine = medicineFacade.getMedicineById(medicineId);
        request.setAttribute("medName", medicine.getName());
        request.setAttribute("medicineId", medicineId);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/adminRemoveMedicinePage.jsp");
        dispatcher.forward(request, response);
    }

    private void redirectAddMedicine(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Supplier> supplierList = supplierFacade.findAll();
        request.setAttribute("supplierList", supplierList);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/adminAddMedicinePage.jsp");
        dispatcher.forward(request, response);
    }
}