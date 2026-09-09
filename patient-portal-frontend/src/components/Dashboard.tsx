import React from 'react';

const Dashboard: React.FC = () => {
  const patientData = {
    name: 'John Doe',
    age: 45,
    upcomingAppointments: [
      { id: 1, date: '2026-09-15', time: '10:00 AM', doctor: 'Dr. Smith' },
      { id: 2, date: '2026-10-02', time: '02:30 PM', doctor: 'Dr. Jones' },
    ],
    recentLabs: [
      { id: 1, test: 'Blood Glucose', result: '95 mg/dL', status: 'Normal' },
      { id: 2, test: 'Cholesterol', result: '190 mg/dL', status: 'Normal' },
    ],
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <h1>Patient Dashboard</h1>
      <section style={{ marginBottom: '30px', border: '1px solid #ddd', padding: '15px', borderRadius: '8px' }}>
        <h2>Profile Information</h2>
        <p><strong>Name:</strong> {patientData.name}</p>
        <p><strong>Age:</strong> {patientData.age}</p>
      </section>

      <section style={{ marginBottom: '30px' }}>
        <h2>Upcoming Appointments</h2>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: '#f4f4f4' }}>
              <th style={{ border: '1px solid #ddd', padding: '8px' }}>Date</th>
              <th style={{ border: '1px solid #ddd', padding: '8px' }}>Time</th>
              <th style={{ border: '1px solid #ddd', padding: '8px' }}>Doctor</th>
            </tr>
          </thead>
          <tbody>
            {patientData.upcomingAppointments.map((app) => (
              <tr key={app.id}>
                <td style={{ border: '1px solid #ddd', padding: '8px' }}>{app.date}</td>
                <td style={{ border: '1px solid #ddd', padding: '8px' }}>{app.time}</td>
                <td style={{ border: '1px solid #ddd', padding: '8px' }}>{app.doctor}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section>
        <h2>Recent Lab Results</h2>
        <ul style={{ listStyleType: 'none', padding: 0 }}>
          {patientData.recentLabs.map((lab) => (
            <li key={lab.id} style={{ marginBottom: '10px', padding: '10px', backgroundColor: '#f9f9f9', borderLeft: '5px solid #4CAF50' }}>
              <strong>{lab.test}:</strong> {lab.result} ({lab.status})
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
};

export default Dashboard;
