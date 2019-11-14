package kr.pe.tocgic.tools.strresparser.view;

import kr.pe.tocgic.tools.strresparser.StResManager;
import kr.pe.tocgic.tools.strresparser.data.enums.ExportXlsColumn;
import kr.pe.tocgic.tools.strresparser.data.enums.Language;
import kr.pe.tocgic.tools.strresparser.util.StringUtil;
import kr.pe.tocgic.tools.strresparser.view.data.EnvProperty;
import kr.pe.tocgic.tools.strresparser.view.data.model.StringPath;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.List;

public class MainView extends JFrame {
    private JTextField txtStrPath;
    private JTextField txtSelectOutputPath;
    private DefaultListModel<StringPath> listModel;

    private EnvProperty envProperty;

    private StResManager manager = new StResManager();


    /**
     * main view
     */
    public MainView() {
        super("StrResParser");
        // setting
        init();
        initUI();
    }

    private void init() {
        envProperty = new EnvProperty();
        listModel = new DefaultListModel<>();

        txtStrPath = new JTextField(20);
        txtSelectOutputPath = new JTextField(20);

        loadEnv();
    }

    private void loadEnv() {
        txtSelectOutputPath.setText(envProperty.getOutPath());
        List<StringPath> list = envProperty.getStringPaths();
        for (StringPath item : list) {
            addListModel(item);
        }
    }

    /**
     * UI init
     */
    private void initUI() {
        JButton btnSelectStrPath;
        JButton btnAddStrPath;
        JButton btnLoadResource;
        JButton btnImportXls;
        JButton btnImportXml;
        JButton btnExport;
        JButton btnSelectOutput;
        final JComboBox<Language> comboLanguage = new JComboBox<>(Language.values());
        final JList<StringPath> list = new JList<>(listModel);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(container);

        JLabel label;
        JPanel panel, subPanel;

        ////////// [String Resource Picker]
        label = new JLabel("문자열 리소스 경로 설정"); //label.setPreferredSize(dimLabel);
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int selectedIndex = list.locationToIndex(e.getPoint());
                    ListModel<StringPath> model = list.getModel();
                    StringPath selectedItem = model.getElementAt(selectedIndex);
                    int input = JOptionPane.showConfirmDialog(container, String.format("선택된 아이템을 삭제할까요?\n%s", selectedItem));
                    if (input == JOptionPane.OK_OPTION) {
                        if (envProperty.removeStrPathItem(selectedItem)) {
                            listModel.remove(selectedIndex);
                        }
                    }
                }
                super.mousePressed(e);
            }
        };
        list.setVisibleRowCount(10);
        list.addMouseListener(mouseListener);
        btnSelectStrPath = new JButton("리소스 경로 지정");
        btnSelectStrPath.addActionListener(e -> {
            try {
                txtStrPath.setText(browseDirectory());
            } catch (Exception e1) {
                showMessageDialog(e1.getMessage());
            }
        });
        btnAddStrPath = new JButton("추가");
        btnAddStrPath.addActionListener(e -> {
            try {
                boolean result = addStrPathItem((Language)comboLanguage.getSelectedItem(), txtStrPath.getText());
                if (result) {
                    txtStrPath.setText("");
                }
            } catch (Exception e1) {
                showMessageDialog(e1.getMessage());
            }
        });
        btnLoadResource = new JButton("문자열 리소스 불러오기");
        btnLoadResource.addActionListener(e -> {
            try {
                loadResources();
            } catch (Exception e1) {
                showMessageDialog(e1.getMessage());
            }
        });

        panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        panel.add(label, BorderLayout.NORTH);
        panel.add(comboLanguage, BorderLayout.WEST);
        subPanel = new JPanel();
        subPanel.setLayout(new BorderLayout(0, 0));
        subPanel.add(txtStrPath, BorderLayout.CENTER);
        subPanel.add(btnSelectStrPath, BorderLayout.EAST);
        panel.add(subPanel, BorderLayout.CENTER);
        panel.add(btnAddStrPath, BorderLayout.EAST);
        panel.add(new JScrollPane(list), BorderLayout.SOUTH);
        container.add(panel);
        subPanel = new JPanel();
        subPanel.setLayout(new BorderLayout());
        subPanel.add(btnLoadResource, BorderLayout.EAST);
        container.add(subPanel);

        ////////// [import xls, xml]
        label = new JLabel("데이터 가져오기 (xls 파일이나, xml 로 부터 데이터를 반영)"); //label.setPreferredSize(dimLabel);
        btnImportXls = new JButton("Import xls file");
        btnImportXls.addActionListener(e -> doImportXls());
        btnImportXml = new JButton("Import XML file");
        btnImportXml.addActionListener(e -> doImportXml());
        panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        panel.add(label, BorderLayout.NORTH);
        subPanel = new JPanel();
        subPanel.setLayout(new FlowLayout());
        subPanel.add(btnImportXls);
        subPanel.add(btnImportXml);
        panel.add(subPanel, BorderLayout.CENTER);
        container.add(panel);

        ////////// [export xls, xml]
        panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        label = new JLabel("데이터 내보내기 (xls, xml 로 데이터를 반영)"); //label.setPreferredSize(dimLabel);
        panel.add(label, BorderLayout.NORTH);
        btnSelectOutput = new JButton("out 경로 지정");
        btnSelectOutput.addActionListener(e -> {
            try {
                envProperty.setOutPath(browseDirectory());
                txtSelectOutputPath.setText(envProperty.getOutPath());
            } catch (Exception e1) {
                showMessageDialog(e1.getMessage());
            }
        });
        btnExport = new JButton("Export");
        btnExport.addActionListener(e -> doExport(txtSelectOutputPath.getText()));
        subPanel = new JPanel();
        subPanel.setLayout(new BorderLayout(0, 0));
        subPanel.add(txtSelectOutputPath, BorderLayout.CENTER);
        subPanel.add(btnSelectOutput, BorderLayout.EAST);
        panel.add(subPanel, BorderLayout.CENTER);
        panel.add(btnExport, BorderLayout.EAST);
        container.add(panel);


        Dimension dimFrame = new Dimension(800, 430);
        setSize(dimFrame);
        setMinimumSize(dimFrame);
        pack();
        setLocationCenter();
        setVisible(true);
    }

    /**
     * set location center
     */
    private void setLocationCenter() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((dim.width/2)-(getWidth()/2), (dim.height/2)-(getHeight()/2));
    }

    /**
     * 문자열 리소스 경로 추가
     * @param language language instance
     * @param source string resource directory path
     * @throws Exception error message
     */
    private boolean addStrPathItem(Language language, String source) throws Exception {
        if (language == null) {
            throw new Exception("Language 값이 없습니다.");
        }
        if (StringUtil.isEmpty(source)) {
            throw new Exception("리소스 경로가 지정되지 않았습니다.");
        }
        File file = new File(source);
        if (!file.exists() || !file.canWrite() || !file.canRead()) {
            throw new Exception(String.format("대상 경로가 없거나, (읽기/쓰기)권한이 없습니다. [%s]", source));
        }
        StringPath item = new StringPath(language, source);
        if (envProperty.addStrPathItem(item)) {
            addListModel(item);
            return true;
        }
        return false;
    }

    private void addListModel(StringPath item) {
        listModel.addElement(item);
    }

    /**
     * browse File
     * @return file path
     * @throws Exception error message
     */
    private String browseFile() throws Exception {
        String result = "";

        JFileChooser c = new JFileChooser();
        c.setDialogTitle("Select file");

        int rVal = c.showOpenDialog(MainView.this);
        if (rVal == JFileChooser.APPROVE_OPTION) {
            result = c.getCurrentDirectory().toString() + File.separator + c.getSelectedFile().getName();
        }
        if (rVal == JFileChooser.CANCEL_OPTION) {
        	throw new Exception("사용자 취소");
        }
        return result;
    }

    /**
     * browse Directory
     * @return folder path
     * @throws Exception error message
     */
    private String browseDirectory() throws Exception {
        String result = "";

        JFileChooser c = new JFileChooser();
        c.setDialogTitle("Select Directory");
        c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int rVal = c.showOpenDialog(MainView.this);
        if (rVal == JFileChooser.APPROVE_OPTION) {
            result = c.getCurrentDirectory().toString() + File.separator + c.getSelectedFile().getName();
        }
        if (rVal == JFileChooser.CANCEL_OPTION) {
            throw new Exception("사용자 취소");
        }
        return result;
    }

    /**
     * Resource Load
     * @throws Exception error message
     */
    private void loadResources() throws Exception {
        //init resource path
        manager.clearSourceDirInfoList();
        List<StringPath> list = envProperty.getStringPaths();
        for (StringPath item : list) {
            manager.addResourcePath(item.getLanguage(), item.getPath());
        }

        boolean result = manager.doLoadResources();
        if (!result) {
            throw new Exception("String Resource 일기 실패 : manager.doLoadResources()");
        }
    }

    /**
     * Write, platform string resources
     * @throws Exception error message
     */
    private void writeResources() throws Exception {
        boolean result = manager.doWriteResources();
        if (!result) {
            throw new Exception("String Resource 일기 실패 : manager.doLoadResources()");
        }

    }

    /**
     * update, string resource from xlsx file
     * @param target xlsx file
     * @throws Exception error message
     */
    private void importFromExcel(File target) throws Exception {
        manager.importFromExcel(target, true);
    }

    /**
     * export, updated union string resource to XML (fileName : makeUnionStResXml.xml)
     * @param targetDir target
     * @return isSuccess
     * @throws Exception error message
     */
    private boolean makeUnionStResXml(File targetDir) throws Exception {
        String absoluteFilePath = targetDir.getAbsolutePath() + File.separator + "makeUnionStResXml.xml";
        return manager.makeUnionStResXml(new File(absoluteFilePath), true);
    }

    /**
     * export, updated union string resource to Excel (fileName : makeExcel.xlsx)
     * @param targetDir target
     * @return isSuccess
     * @throws Exception error message
     */
    private boolean makeExcel(File targetDir) throws Exception {
        String absoluteFilePath = targetDir.getAbsolutePath() + File.separator + "makeExcel.xlsx";
        ExportXlsColumn[] columns2 = {ExportXlsColumn.HIDDEN_KEYS, ExportXlsColumn.LANGUAGE_KO, ExportXlsColumn.LANGUAGE_JA, ExportXlsColumn.LANGUAGE_EN};
        return manager.makeExcel(new File(absoluteFilePath), true, columns2, true);
    }

    /**
     * Export
     * 파일 검증 > xls, xml 파일 생성
     * @param targetPath out directory path
     */
    private void doExport(String targetPath) {
        try {
            if (StringUtil.isEmpty(targetPath)) {
                throw new Exception("out 경로가 지정되지 않았습니다.");
            }
            File target = new File(targetPath);
            if (!target.isDirectory() || !target.canWrite()) {
                throw new Exception("out 경로가 디렉토리가 아니거나, 저장 권한이 없습니다.");
            }
            boolean xmlResult = makeUnionStResXml(target);
            boolean xlsResult = makeExcel(target);
            if (xmlResult && xlsResult) {
                showMessageDialog("파일이 out 경로에 생성되어있습니다.");
            } else {
                showMessageDialog(String.format("파일생성 실패. xml:%b, xls:%b", xmlResult, xlsResult));
            }
        } catch (Exception e) {
            showMessageDialog(e.getMessage());
        }
    }

    /**
     * Import from xml
     * 파일 선택 > 파일 검증 > 리소스 로드 > xls 반영 > 리소스 반영
     */
    private void doImportXml() {
        try {
            throw new Exception("기능 준비중...");
            // FIXME: 2019/11/13 구현 예정
        } catch (Exception e) {
            showMessageDialog(e.getMessage());
        }
    }

    /**
     * Import from xls
     * 파일 선택 > 파일 검증 > 리소스 로드 > xls 반영 > 리소스 반영
     */
    private void doImportXls() {
        try {
            //파일 선택
            String sourcePath = browseFile();
            if (StringUtil.isEmpty(sourcePath)) {
                throw new Exception("xls 경로가 지정되지 않았습니다.");
            }
            //파일 검증
            File source = new File(sourcePath);
            if (!source.isFile() || !source.canWrite()) {
                throw new Exception("xls 경로가 파일이 아니거나, 읽기 권한이 없습니다.");
            }
            //리소스 로드
            loadResources();

            //xls import
            importFromExcel(source);

            //리소스 반영
            writeResources();
        } catch (Exception e) {
            showMessageDialog(e.getMessage());
        }
    }

    /**
     * show message dialog
     * @param message message
     */
    private void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
