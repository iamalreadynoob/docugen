import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Screen extends JFrame
{

    private JButton convert;
    private JTextField result;

    public Screen()
    {
        this.setSize(500, 300);
        this.setTitle("Docugen");
        this.setResizable(false);
        this.setLayout(null);
        this.getContentPane().setBackground(Color.BLACK);

        createNew();
        add();
        positions();
        buttons();

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private void createNew()
    {
        convert = new JButton();
        result = new JTextField();
    }

    private void add()
    {
        this.add(convert);
        this.add(result);
    }

    private void positions()
    {
        convert.setBounds(200, 60, 100, 30);
        convert.setBorder(null);
        convert.setText("convert");
        convert.setBackground(Color.decode("#204378"));
        convert.setForeground(Color.WHITE);

        result.setBounds(100, 150, 300, 30);
        result.setBackground(null);
        result.setForeground(Color.WHITE);
        result.setHorizontalAlignment(SwingConstants.CENTER);
        result.setEditable(false);
    }

    private void buttons()
    {
        convert.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser chooser = new JFileChooser();
                int value = chooser.showOpenDialog(null);

                if (value == JFileChooser.APPROVE_OPTION)
                {
                    try
                    {
                        new Process(chooser.getSelectedFile().getAbsolutePath());
                        result.setText("Markdown file has been created successfully.");
                    }
                    catch (Exception ex) {result.setText(ex.getMessage());}
                }
            }

        });
    }
}
