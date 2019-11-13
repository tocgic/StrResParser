package kr.pe.tocgic.tools.strresparser.view;

import kr.pe.tocgic.tools.strresparser.StResManager;
import kr.pe.tocgic.tools.strresparser.data.enums.ExportXlsColumn;
import kr.pe.tocgic.tools.strresparser.data.enums.Language;
import kr.pe.tocgic.tools.strresparser.util.StringUtil;
import kr.pe.tocgic.tools.strresparser.view.model.StringPath;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

public class MainView extends JFrame {
    private JButton btnSelectStrPath;
    private JButton btnAddStrPath;
    private JButton btnLoadResource;
    private JButton btnImportXls;
    private JButton btnImportXml;
    private JButton btnExport;
    private JButton btnSelectOutput;

    private JComboBox<Language> comboLanguage;
    private JList<StringPath> list;
    private DefaultListModel<StringPath> listModel = new DefaultListModel<>();

    private JTextField txtStrPath;
    private JTextField txtSelectOutputPath;

    private StResManager manager = new StResManager();


    /** main view **/
    public MainView() {
        // setting
        super("StrResParser");
        init();
        initUI();
        loadEnv();
    }

    private void loadEnv() {
        try {
            txtSelectOutputPath.setText("/Users/tocgic/Temp/strresparser");

        } catch (Exception e) {
            showMessageDialog(e.getMessage());
        }
    }

    private void init() {
    }

    private void initUI() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(container);

        JLabel label;
        JPanel panel, subPanel;
        Dimension dimLabel = new Dimension(160, 30);
        Dimension dimButton = null; //new Dimension(200, 30);

        ////////// [String Resource Picker]
        label = new JLabel("문자열 리소스 경로 설정"); //label.setPreferredSize(dimLabel);
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int selectedIndex = list.locationToIndex(e.getPoint());
                    ListModel<StringPath> model = list.getModel();
                    int input = JOptionPane.showConfirmDialog(container, String.format("선택된 아이템을 삭제할까요?\n%s", model.getElementAt(selectedIndex)));
                    if (input == JOptionPane.OK_OPTION) {
                        listModel.remove(selectedIndex);
                    }
                }
                super.mousePressed(e);
            }
        };
        list = new JList<>(listModel);
        list.setVisibleRowCount(10);
        list.addMouseListener(mouseListener);
        comboLanguage = new JComboBox<>(Language.values());
        txtStrPath = new JTextField(20);
        btnSelectStrPath = new JButton("찾기");
        btnSelectStrPath.setMinimumSize(dimButton);
        btnSelectStrPath.addActionListener(e -> {
            try {
                txtStrPath.setText(browseDirectory());
            } catch (Exception e1) {
                showMessageDialog(e1.getMessage());
            }
        });
        btnAddStrPath = new JButton("추가");
        btnAddStrPath.setMinimumSize(dimButton);
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
        btnLoadResource.setMinimumSize(dimButton);
        btnLoadResource.addActionListener(e -> {
            try {
                loadResources();
            } catch (Exception e1) {
                showMessageDialog(e1.getMessage());
            }
        });

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(comboLanguage, BorderLayout.WEST);
        panel.add(txtStrPath, BorderLayout.CENTER);
        subPanel = new JPanel();
        subPanel.setLayout(new FlowLayout());
        subPanel.add(btnSelectStrPath);
        subPanel.add(btnAddStrPath);
        panel.add(subPanel, BorderLayout.EAST);
        panel.add(new JScrollPane(list), BorderLayout.SOUTH);
        container.add(panel);
        subPanel = new JPanel();
        subPanel.setLayout(new BorderLayout());
        subPanel.add(btnLoadResource, BorderLayout.EAST);
        container.add(subPanel);

        ////////// [import xls, xml]
        label = new JLabel("데이터 가져오기 (xls 파일이나, xml 로 부터 데이터를 반영)"); //label.setPreferredSize(dimLabel);
        btnImportXls = new JButton("Import xls file");
        btnImportXls.setMinimumSize(dimButton);
        btnImportXls.addActionListener(e -> doImportXls());
        btnImportXml = new JButton("Import XML file");
        btnImportXml.setMinimumSize(dimButton);
        btnImportXml.addActionListener(e -> doImportXml());
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        subPanel = new JPanel();
        subPanel.setLayout(new FlowLayout());
        subPanel.add(btnImportXls);
        subPanel.add(btnImportXml);
        panel.add(subPanel, BorderLayout.CENTER);
        container.add(panel);

        ////////// [export xls, xml]
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        label = new JLabel("데이터 내보내기 (xls, xml 로 데이터를 반영)"); //label.setPreferredSize(dimLabel);
        panel.add(label, BorderLayout.NORTH);
        txtSelectOutputPath = new JTextField(20);
        btnSelectOutput = new JButton("out 경로 지정");
        btnSelectOutput.setMinimumSize(dimButton);
        btnSelectOutput.addActionListener(e -> {
            try {
                txtSelectOutputPath.setText(browseDirectory());
            } catch (Exception e1) {
                showMessageDialog(e1.getMessage());
            }
        });
        btnExport = new JButton("Export");
        btnExport.setMinimumSize(dimButton);
        btnExport.addActionListener(e -> doExport(txtSelectOutputPath.getText()));
        panel.add(txtSelectOutputPath, BorderLayout.CENTER);
        subPanel = new JPanel();
        subPanel.setLayout(new FlowLayout());
        subPanel.add(btnSelectOutput);
        subPanel.add(btnExport);
        panel.add(subPanel, BorderLayout.EAST);
        container.add(panel);


        Dimension dimFrame = new Dimension(660, 210);
        setSize(dimFrame);
        setMinimumSize(dimFrame);
        pack();
        setLocation(800, 450);
        setVisible(true);
    }

    /**
     * 문자열 리소스 경로 추가
     * @param language
     * @param source
     * @throws Exception
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
        listModel.addElement(new StringPath(language, source));
        return true;
    }

    /** browse File **/
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

    /** browse Directory **/
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
     */
    private void loadResources() throws Exception {
        //init resource path
        manager.clearSourceDirInfoList();
        int size = listModel.getSize();
        for (int i = 0; i < size; i++) {
            StringPath item = listModel.get(i);
            manager.addResourcePath(item.getLanguage(), item.getPath());
        }

        boolean result = manager.doLoadResources();
        if (!result) {
            throw new Exception("String Resource 일기 실패 : manager.doLoadResources()");
        }
    }

    /**
     * Write, platform string resources
     */
    private void writeResources() throws Exception {
        boolean result = manager.doWriteResources();
        if (!result) {
            throw new Exception("String Resource 일기 실패 : manager.doLoadResources()");
        }

    }

    /**
     * update, string resource from xlsx file
     */
    private void importFromExcel(File target) throws Exception {
        manager.importFromExcel(target, true);
    }

    /**
     * export, updated union string resource to XML (fileName : makeUnionStResXml.xml)
     */
    private boolean makeUnionStResXml(File targetDir) throws Exception {
        String absoluteFilePath = targetDir.getAbsolutePath() + File.separator + "makeUnionStResXml.xml";
        return manager.makeUnionStResXml(new File(absoluteFilePath), true);
    }

    /**
     * export, updated union string resource to Excel (fileName : makeExcel.xlsx)
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

    private void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
