const { useState, useEffect } = React;

function App() {
    const [file, setFile] = useState(null);
    const [preview, setPreview] = useState(null);
    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState(null);

    const handleFileChange = (e) => {
        const selectedFile = e.target.files[0];
        if (selectedFile) {
            setFile(selectedFile);
            setPreview(URL.createObjectURL(selectedFile));
            setResult(null); // Reset results for new upload
        }
    };

    const handleUpload = async () => {
        if (!file) return;
        setLoading(true);
        
        const formData = new FormData();
        formData.append("image", file);

        try {
            // Send Multipart form data to Spring Boot API running natively via gradlew
            const response = await axios.post("http://localhost:8080/api/analyze", formData, {
                headers: { "Content-Type": "multipart/form-data" }
            });
            setResult(response.data);
        } catch (error) {
            console.error("Error uploading photo:", error);
            alert("Failed to reach Spring Boot Backend! Make sure it is running on :8080 and MySQL is active.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <React.Fragment>
            <header className="navbar">
                <div className="logo">Vision<span>AI</span></div>
                <nav>
                    <a href="#">Analysis</a>
                    <a href="#">Suggestions</a>
                </nav>
            </header>

            <main className="container">
                <section className="hero">
                    <div className="hero-content">
                        <div className="tag">Full-Stack React Application</div>
                        <h1>Intelligent Photo Evaluation</h1>
                        <p>Upload your image to be processed by our Spring Boot backend. We'll store it securely in MySQL and return a real-life dynamic analysis score.</p>
                        
                        {!result && (
                            <div className="upload-section">
                                <label className="file-upload-label">
                                    <input type="file" accept="image/*" onChange={handleFileChange} />
                                    {preview ? "Select Different Photo" : "Upload a Photo (JPG/PNG)"}
                                </label>
                                {preview && (
                                    <button className="btn-primary fade-in visible" onClick={handleUpload} disabled={loading}>
                                        {loading ? "Analyzing..." : "Analyze Image Now"}
                                    </button>
                                )}
                            </div>
                        )}
                        
                        {result && (
                            <div className="stats-row fade-in visible">
                                <div className="stat-item">
                                    <span className="stat-value">{result.balanceScore}%</span>
                                    <span className="stat-label">Real-time Score</span>
                                </div>
                                <div className="stat-item">
                                    <span className="stat-value">{result.compositionType}</span>
                                    <span className="stat-label">Composition</span>
                                </div>
                            </div>
                        )}
                    </div>
                    
                    <div className="hero-image-wrapper">
                        <img 
                            src={preview || "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?q=80&w=1200&auto=format&fit=crop"} 
                            alt="Landscape Photo" 
                            className="hero-img" 
                        />
                        {result && <div className="image-overlay-grid"></div>}
                        {result && <div className="focus-point focus-tree"></div>}
                    </div>
                </section>

                {result && (
                    <section className="analysis-section fade-in visible">
                        <h2>Backend Real-life Results (Database ID: {result.id})</h2>
                        <div className="cards-grid">
                            <div className="glass-card">
                                <div className="card-icon">⚖️</div>
                                <h3>Asymmetrical Balance</h3>
                                <p>{result.balanceRemarks}</p>
                            </div>
                            <div className="glass-card">
                                <div className="card-icon">↗️</div>
                                <h3>Leading Lines Calculation</h3>
                                <p>{result.linesRemarks}</p>
                            </div>
                        </div>
                    </section>
                )}
            </main>
        </React.Fragment>
    );
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
